package studio.pixelforge.backend.lecture;

public record LectureCardResponse(Long id, Integer position, CardType cardType, String content) {
    public static LectureCardResponse from(LectureCard c) {
        return new LectureCardResponse(c.getId(), c.getPosition(), c.getCardType(), c.getContent());
    }
}
