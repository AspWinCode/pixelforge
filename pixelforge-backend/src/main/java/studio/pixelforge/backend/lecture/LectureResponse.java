package studio.pixelforge.backend.lecture;

public record LectureResponse(Long id, String title) {
    public static LectureResponse from(Lecture l) {
        return new LectureResponse(l.getId(), l.getTitle());
    }
}
