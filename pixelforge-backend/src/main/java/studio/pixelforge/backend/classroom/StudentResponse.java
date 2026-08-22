package studio.pixelforge.backend.classroom;

import studio.pixelforge.backend.user.User;

public record StudentResponse(Long id, String fullName) {
    public static StudentResponse from(User u) {
        return new StudentResponse(u.getId(), u.getFullName());
    }
}
