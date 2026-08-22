package studio.pixelforge.backend.lecture;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LectureCardRepository extends JpaRepository<LectureCard, Long> {
    List<LectureCard> findByLecture_IdOrderByPositionAsc(Long lectureId);
}
