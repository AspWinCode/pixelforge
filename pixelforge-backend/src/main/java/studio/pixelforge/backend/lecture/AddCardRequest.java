package studio.pixelforge.backend.lecture;

import jakarta.validation.constraints.NotNull;

public record AddCardRequest(@NotNull CardType cardType, String content) {}
