package studio.pixelforge.backend.submission;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    Optional<Submission> findByAssignment_IdAndUser_Id(Long assignmentId, Long userId);

    List<Submission> findByAssignment_Id(Long assignmentId);

    List<Submission> findByUser_IdAndStatus(Long userId, SubmissionStatus status);

    List<Submission> findByUser_Id(Long userId);

    List<Submission> findTop10ByUser_IdOrderByCreatedAtDesc(Long userId);
}
