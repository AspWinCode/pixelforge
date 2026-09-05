package studio.pixelforge.backend.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseNodeRepository extends JpaRepository<CourseNode, Long> {

    List<CourseNode> findByCourse_IdOrderBySortOrderAsc(Long courseId);
}
