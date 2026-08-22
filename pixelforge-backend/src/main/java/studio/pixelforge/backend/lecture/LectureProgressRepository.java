package studio.pixelforge.backend.lecture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LectureProgressRepository extends JpaRepository<LectureProgress, Long> {
    Optional<LectureProgress> findByLecture_IdAndUser_Id(Long lectureId, Long userId);
}
