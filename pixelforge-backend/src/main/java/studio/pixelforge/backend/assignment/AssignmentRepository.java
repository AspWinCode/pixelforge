package studio.pixelforge.backend.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByClassEntity_IdAndStatus(Long classId, AssignmentStatus status);

    List<Assignment> findByClassEntity_Id(Long classId);
}
