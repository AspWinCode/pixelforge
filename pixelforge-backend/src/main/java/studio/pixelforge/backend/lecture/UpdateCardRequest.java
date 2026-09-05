package studio.pixelforge.backend.lecture;

// null — не менять. Спека требует минимум content; cardType опционально.
public record UpdateCardRequest(CardType cardType, String content) {
}
