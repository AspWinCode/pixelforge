package studio.pixelforge.backend.user;

public record UserResponse(Long id, String fullName, UserRole role, boolean onboardingCompleted) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getFullName(), user.getRole(), user.getOnboardingCompleted());
    }
}
