package studio.pixelforge.backend.lecture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    List<Lecture> findByOrganization_Id(Long organizationId);
}
