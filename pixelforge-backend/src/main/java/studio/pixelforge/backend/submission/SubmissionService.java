package studio.pixelforge.backend.submission;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.assignment.AssignmentRepository;
import studio.pixelforge.backend.common.exception.PayloadTooLargeException;
import studio.pixelforge.backend.npc.NpcService;
import studio.pixelforge.backend.storage.S3Service;
import studio.pixelforge.backend.token.TokenService;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class SubmissionService {

    private static final int TOKENS_FOR_REVIEWED_SUBMISSION = 10;
    private static final long MAX_XML_SIZE_BYTES = 5L * 1024 * 1024;
    private static final int REMINDER_THRESHOLD = 5;

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final S3Service s3Service;
    private final NpcService npcService;

    public SubmissionService(SubmissionRepository submissionRepository,
                              AssignmentRepository assignmentRepository,
                              UserRepository userRepository,
                              TokenService tokenService,
                              S3Service s3Service,
                              NpcService npcService) {
        this.submissionRepository = submissionRepository;
        this.assignmentRepository = assignmentRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.s3Service = s3Service;
        this.npcService = npcService;
    }

    @Transactional
    public Submission startOrGet(Long assignmentId, Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Optional<Submission> existing = submissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userId);

        Submission submission;
        if (existing.isPresent()) {
            submission = existing.get();
        } else {
            Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));
            submission = new Submission(assignment, user);
            submission.setStartActivityCount(user.getActivityCount());
            submission = submissionRepository.save(submission);
        }

        user.setActivityCount(user.getActivityCount() + 1);
        userRepository.save(user);

        checkForgottenSubmissions(userId, user.getActivityCount(), assignmentId);

        return submission;
    }

    private void checkForgottenSubmissions(Long userId, int currentActivityCount, Long excludeAssignmentId) {
        List<Submission> inProgress = submissionRepository.findByUser_IdAndStatus(userId, SubmissionStatus.IN_PROGRESS);

        for (Submission s : inProgress) {
            if (s.getAssignment().getId().equals(excludeAssignmentId)) continue;
            if (s.isReminded()) continue;
            if (s.getStartActivityCount() == null) continue;

            int stepsSince = currentActivityCount - s.getStartActivityCount();
            if (stepsSince >= REMINDER_THRESHOLD) {
                npcService.onForgottenSubmission(userId, s.getAssignment().getTitle());
                s.setReminded(true);
            }
        }
    }

    // Только чтение, без побочного эффекта создания — используется дорожкой,
    // чтобы не "начинать" задание одним лишь его отображением в списке пути.
    @Transactional(readOnly = true)
    public Optional<SubmissionStatus> findStatus(Long assignmentId, Long userId) {
        return submissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userId)
            .map(Submission::getStatus);
    }

    @Transactional
    public Submission saveProject(Long assignmentId, Long userId, String xml) {
        long sizeBytes = xml.getBytes(StandardCharsets.UTF_8).length;
        if (sizeBytes > MAX_XML_SIZE_BYTES) {
            throw new PayloadTooLargeException(
                "Project file is too large: " + (sizeBytes / 1024 / 1024) + "MB, max allowed is 5MB"
            );
        }

        Submission submission = submissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Submission not found for this assignment/user"));

        String key = "submissions/" + submission.getId() + ".xml";
        s3Service.uploadXml(key, xml);
        submission.setS3Key(key);

        return submission;
    }

    @Transactional(readOnly = true)
    public Optional<String> getSavedProjectXml(Long assignmentId, Long userId) {
        Submission submission = submissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Submission not found for this assignment/user"));

        if (submission.getS3Key() == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(s3Service.downloadXml(submission.getS3Key()));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Transactional(readOnly = true)
    public List<Submission> listByAssignment(Long assignmentId) {
        return submissionRepository.findByAssignment_Id(assignmentId);
    }

    @Transactional
    public Submission submit(Long assignmentId, Long userId) {
        Submission submission = submissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Submission not found for this assignment/user"));

        if (submission.getStatus() == SubmissionStatus.IN_PROGRESS) {
            submission.setStatus(SubmissionStatus.SUBMITTED);
            npcService.onAssignmentSubmitted(userId, submission.getAssignment().getTitle());
        }

        return submission;
    }

    @Transactional
    public Submission review(Long assignmentId, Long userId) {
        Submission submission = submissionRepository.findByAssignment_IdAndUser_Id(assignmentId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Submission not found for this assignment/user"));

        if (submission.getStatus() != SubmissionStatus.SUBMITTED) {
            throw new IllegalStateException(
                "Cannot review submission in status " + submission.getStatus() + " — must be SUBMITTED first"
            );
        }

        submission.setStatus(SubmissionStatus.REVIEWED);

        tokenService.award(
            userId,
            TOKENS_FOR_REVIEWED_SUBMISSION,
            "assignment_reviewed",
            Map.of("assignmentId", assignmentId, "submissionId", submission.getId())
        );

        npcService.onTokensAwarded(userId, TOKENS_FOR_REVIEWED_SUBMISSION);

        return submission;
    }
}
