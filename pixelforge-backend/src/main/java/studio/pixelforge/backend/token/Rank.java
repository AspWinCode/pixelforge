package studio.pixelforge.backend.token;

// Пороги — сколько токенов нужно накопить, чтобы получить звание.
// При начислении 10 токенов за проверенное задание это соответствует
// примерно 3/8/15/25/50 сданным заданиям — разумная прогрессия для месяца работы.
public enum Rank {
    JUNIOR_DEV(0, "Junior Dev"),
    MID_DEV(50, "Mid Dev"),
    SENIOR_DEV(150, "Senior Dev"),
    LEAD_DEV(300, "Lead Dev"),
    CTO(500, "CTO");

    private final int minBalance;
    private final String displayName;

    Rank(int minBalance, String displayName) {
        this.minBalance = minBalance;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Идём от старшего звания к младшему и берём первое, чей порог пройден —
    // проще и надёжнее, чем накопительно сравнивать соседние диапазоны.
    public static Rank fromBalance(long balance) {
        Rank[] all = values();
        for (int i = all.length - 1; i >= 0; i--) {
            if (balance >= all[i].minBalance) {
                return all[i];
            }
        }
        return JUNIOR_DEV;
    }
}
