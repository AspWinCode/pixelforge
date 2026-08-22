package studio.pixelforge.backend.assignment;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.classroom.ClassEntity;
import studio.pixelforge.backend.classroom.ClassEntityRepository;
import studio.pixelforge.backend.lecture.Lecture;
import studio.pixelforge.backend.lecture.LectureRepository;
import studio.pixelforge.backend.storage.S3Service;

import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final AssignmentImageRepository assignmentImageRepository;
    private final LectureRepository lectureRepository;
    private final S3Service s3Service;

    public AssignmentService(AssignmentRepository assignmentRepository,
                              ClassEntityRepository classEntityRepository,
                              AssignmentImageRepository assignmentImageRepository,
                              LectureRepository lectureRepository,
                              S3Service s3Service) {
        this.assignmentRepository = assignmentRepository;
        this.classEntityRepository = classEntityRepository;
        this.assignmentImageRepository = assignmentImageRepository;
        this.lectureRepository = lectureRepository;
        this.s3Service = s3Service;
    }

    @Transactional
    public Assignment create(CreateAssignmentRequest request) {
        ClassEntity classEntity = classEntityRepository.findById(request.classId())
            .orElseThrow(() -> new EntityNotFoundException("Class not found: " + request.classId()));

        // lectureId необязателен — задание может существовать без привязанной теории.
        Lecture lecture = null;
        if (request.lectureId() != null) {
            lecture = lectureRepository.findById(request.lectureId())
                .orElseThrow(() -> new EntityNotFoundException("Lecture not found: " + request.lectureId()));
        }

        return assignmentRepository.save(
            new Assignment(classEntity, lecture, request.title(), request.description(), request.tool(), request.deadline())
        );
    }

    @Transactional
    public Assignment publish(Long assignmentId) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));

        assignment.setStatus(AssignmentStatus.PUBLISHED);
        return assignment;
    }

    @Transactional(readOnly = true)
    public Assignment getById(Long id) {
        return assignmentRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Assignment> listPublishedForClass(Long classId) {
        return assignmentRepository.findByClassEntity_IdAndStatus(classId, AssignmentStatus.PUBLISHED);
    }

    @Transactional(readOnly = true)
    public List<Assignment> listAllForClass(Long classId) {
        return assignmentRepository.findByClassEntity_Id(classId);
    }

    @Transactional
    public AssignmentImage addImage(Long assignmentId, String originalName, byte[] content, String contentType) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new EntityNotFoundException("Assignment not found: " + assignmentId));

        String key = "assignment-images/" + assignmentId + "/" + UUID.randomUUID() + "-" + originalName;
        s3Service.uploadBytes(key, content, contentType);

        String safeContentType = (contentType != null) ? contentType : "application/octet-stream";
        return assignmentImageRepository.save(new AssignmentImage(assignment, key, originalName, safeContentType));
    }

    @Transactional(readOnly = true)
    public List<AssignmentImage> listImages(Long assignmentId) {
        return assignmentImageRepository.findByAssignment_Id(assignmentId);
    }

    @Transactional(readOnly = true)
    public AssignmentImage getImage(Long imageId) {
        return assignmentImageRepository.findById(imageId)
            .orElseThrow(() -> new EntityNotFoundException("Image not found: " + imageId));
    }

    @Transactional(readOnly = true)
    public byte[] getImageBytes(String s3Key) {
        return s3Service.downloadBytes(s3Key);
    }
}
