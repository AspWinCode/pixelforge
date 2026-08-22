package studio.pixelforge.backend.classroom;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import studio.pixelforge.backend.user.User;

import java.time.Instant;

@Entity
@Table(name = "class_members")
@IdClass(ClassMemberId.class)
@Getter
@NoArgsConstructor
public class ClassMember {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassEntity classEntity;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt;

    @PrePersist
    void onCreate() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
    }

    public ClassMember(ClassEntity classEntity, User user) {
        this.classEntity = classEntity;
        this.user = user;
    }
}
