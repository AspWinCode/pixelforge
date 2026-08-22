package studio.pixelforge.backend.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import studio.pixelforge.backend.organization.Organization;

import java.time.Instant;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(columnNames = {"org_id", "lms_user_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(name = "lms_user_id", nullable = false)
    private String lmsUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    // Растёт при каждом открытии задания — используется, чтобы понять,
    // сколько заданий ученик успел начать, не закончив предыдущее.
    @Column(name = "activity_count", nullable = false)
    private Integer activityCount = 0;

    @Column(name = "onboarding_completed", nullable = false)
    private Boolean onboardingCompleted = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public User(Organization organization, String lmsUserId, UserRole role, String fullName) {
        this.organization = organization;
        this.lmsUserId = lmsUserId;
        this.role = role;
        this.fullName = fullName;
    }
}
