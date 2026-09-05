package studio.pixelforge.backend.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NodeTaskRepository extends JpaRepository<NodeTask, Long> {

    List<NodeTask> findByNode_IdOrderBySortOrderAsc(Long nodeId);

    List<NodeTask> findByNode_Course_IdOrderBySortOrderAsc(Long courseId);

    Optional<NodeTask> findByNode_IdAndAssignment_Id(Long nodeId, Long assignmentId);
}
