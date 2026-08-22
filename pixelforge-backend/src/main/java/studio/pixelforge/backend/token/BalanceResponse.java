package studio.pixelforge.backend.token;

public record BalanceResponse(Long userId, long balance, Rank rank, String rankDisplayName) {}
