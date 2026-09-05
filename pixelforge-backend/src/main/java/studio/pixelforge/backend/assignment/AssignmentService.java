package studio.pixelforge.backend.assignment;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.classroom.ClassEntity;
import studio.pixelforge.backend.classroom.ClassEntityRepository;
import studio.pixelforge.backend.lecture.Lecture;
import studio.pixelforge.backend.lecture.LectureRepository;
import studio.pixelforge.backend.storage.S3Service;
import studio.pixelforge.backend.submission.SubmissionRepository;

import java.util.List;
import java.util.UUID;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final ClassEntityRepository classEntityRepository;
    private final AssignmentImageRepository assignmentImageRepository;
    private final LectureRepository lectureRepository;
    private final SubmissionRepository submissionRepository;
    private final S3Service s3Service;

    public AssignmentService(AssignmentRepository assignmentRepository,
                              ClassEntityRepository classEntityRepository,
                              AssignmentImageRepository assignmentImageRepository,
                              LectureRepository lectureRepository,
                              SubmissionRepository submissionRepository,
                              S3Service s3Service) {
        this.assignmentRepository = assignmentRepository;
        this.classEntityRepository = classEntityRepository;
        this.assignmentImageRepository = assignmentImageRepository;
        this.lectureRepository = lectureRepository;
        this.submissionRepository = submissionRepository;
        this.s3Service = s3Service;
    }

    // Задача-шаблон студии методиста: без класса, DRAFT. Привязка к узлу
    // дерева — через NodeTaskService.
    @Transactional
    public Assignment createTemplate(String title, AssignmentTool tool) {
        return assignmentRepository.save(new Assignment(title, tool));
    }

    @Transactional
    public Assignment update(Long id, UpdateTaskRequest request) {
        Assignment assignment = getById(id);
        if (request.title() != null) {
            assignment.setTitle(request.title());
        }
        if (request.description() != null) {
            assignment.setDescription(request.description());
        }
        if (request.tool() != null) {
            assignment.setTool(request.tool());
        }
        if (request.deadline() != null) {
            assignment.setDeadline(request.deadline());
        }
        if (request.lectureId() != null) {
            Lecture lecture = lectureRepository.findById(request.lectureId())
                .orElseThrow(() -> new EntityNotFoundException("Lecture not found: " + request.lectureId()));
            assignment.setLecture(lecture);
        }
        if (request.classId() != null) {
            ClassEntity classEntity = classEntityRepository.findById(request.classId())
                .orElseThrow(() -> new EntityNotFoundException("Class not found: " + request.classId()));
            assignment.setClassEntity(classEntity);
        }
        return assignment;
    }

    @Transactional
    public void delete(Long id) {
        Assignment assignment = getById(id);
        if (!submissionRepository.findByAssignment_Id(id).isEmpty()) {
            throw new IllegalStateException("Cannot delete a task that already has submissions");
        }
        // node_task снимется каскадом (ON DELETE CASCADE). Картинки удаляем
        // явно — S3-объекты остаются (отдельная задача по очистке хранилища).
        assignmentImageRepository.deleteAll(assignmentImageRepository.findByAssignment_Id(id));
        assignmentRepository.delete(assignment);
    }

    @Transactional
    public Assignment publish(Long assignmentId) {
        Assignment assignment = getById(assignmentId);
        assignment.setStatus(AssignmentStatus.PUBLISHED);
        return assignment;
    }

    @Transactional
    public Assignment unpublish(Long assignmentId) {
        Assignment assignment = getById(assignmentId);
        assignment.setStatus(AssignmentStatus.DRAFT);
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
        Assignment assignment = getById(assignmentId);

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
