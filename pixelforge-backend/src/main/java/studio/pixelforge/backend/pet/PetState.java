package studio.pixelforge.backend.pet;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studio.pixelforge.backend.user.User;

import java.time.Instant;

@Entity
@Table(name = "pet_states")
@Getter
@Setter
@NoArgsConstructor
public class PetState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    private Integer hunger;
    private Integer mood;
    private Integer energy;
    private Integer level;

    // Ребёнок называет питомца на онбординге; до этого момента остаётся
    // null — фронт сам решает, каким дефолтом подписать карточку.
    private String name;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        if (hunger == null) hunger = 100;
        if (mood == null) mood = 100;
        if (energy == null) energy = 100;
        if (level == null) level = 1;
        updatedAt = Instant.now();
    }

    public PetState(User user) {
        this.user = user;
    }

    // Зажимаем значение в диапазон [0, 100] — характеристики не могут
    // выйти за границы ни вверх (перекормить нельзя больше "полностью сыт"),
    // ни вниз (не может быть отрицательного голода).
    public static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }
}
