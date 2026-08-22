package studio.pixelforge.backend.token;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import studio.pixelforge.backend.organization.Organization;
import studio.pixelforge.backend.user.User;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "token_events")
@Getter
@NoArgsConstructor
public class TokenEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "org_id", nullable = false)
    private Organization organization;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private String reason;

    // JSONB-колонка. Храним произвольный контекст события
    // (например: {"assignmentId": 1, "submissionId": 5}) без отдельной таблицы под это.
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> meta;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public TokenEvent(User user, Organization organization, Integer amount, String reason, Map<String, Object> meta) {
        this.user = user;
        this.organization = organization;
        this.amount = amount;
        this.reason = reason;
        this.meta = meta;
    }
}
