package studio.pixelforge.backend.lms;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// Наш собственный предложенный контракт — LMS ещё не подтвердила формат,
// это черновик для обсуждения (как мы делали с JWT-payload).
public record SyncClassRequest(
    @NotBlank String lmsClassId,
    @NotBlank String className,
    @NotEmpty List<@Valid Member> members
) {
    public record Member(
        @NotBlank String lmsUserId,
        @NotBlank String fullName,
        @NotBlank String role
    ) {}
}
