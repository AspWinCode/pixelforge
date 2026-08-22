package studio.pixelforge.backend.classroom;

import java.io.Serializable;
import java.util.Objects;

// Составной ключ для class_members: (class_id, user_id).
// Обязателен для JPA, когда у Entity нет своего единственного @Id,
// а первичный ключ состоит из нескольких колонок.
public class ClassMemberId implements Serializable {

    private Long classEntity;
    private Long user;

    public ClassMemberId() {}

    public ClassMemberId(Long classEntity, Long user) {
        this.classEntity = classEntity;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassMemberId that)) return false;
        return Objects.equals(classEntity, that.classEntity) && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classEntity, user);
    }
}
