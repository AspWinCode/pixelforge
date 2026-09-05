package studio.pixelforge.backend.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    Optional<CourseEnrollment> findByCourse_IdAndUser_Id(Long courseId, Long userId);

    boolean existsByCourse_IdAndUser_Id(Long courseId, Long userId);

    List<CourseEnrollment> findByCourse_IdOrderByEnrolledAtAsc(Long courseId);

    List<CourseEnrollment> findByUser_Id(Long userId);
}
