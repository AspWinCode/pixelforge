package studio.pixelforge.backend.auth;

import studio.pixelforge.backend.user.UserRole;

import java.io.Serializable;

// Хранится как атрибут HttpSession — сессии лежат в Redis
// (spring.session.store-type=redis), поэтому объект обязан быть
// сериализуемым. Специально маленький и плоский: только то, что нужно,
// чтобы на каждый запрос не ходить в БД за пользователем заново.
public record SessionUser(Long userId, UserRole role, String fullName) implements Serializable {
}
