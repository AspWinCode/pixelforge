package studio.pixelforge.backend.assignment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface TaskTestRepository extends JpaRepository<TaskTest, Long> {
    List<TaskTest> findByAssignment_IdOrderByOrderIndexAsc(Long assignmentId);
}

interface TaskHintRepository extends JpaRepository<TaskHint, Long> {
    List<TaskHint> findByAssignment_IdOrderByOrderIndexAsc(Long assignmentId);
}
