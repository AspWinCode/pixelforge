package studio.pixelforge.backend.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentImageRepository extends JpaRepository<AssignmentImage, Long> {

    List<AssignmentImage> findByAssignment_Id(Long assignmentId);
}
