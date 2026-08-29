package studio.pixelforge.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record LmsSsoRequest(@NotBlank String token) {
}
