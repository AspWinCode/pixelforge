package studio.pixelforge.backend.course;

import jakarta.validation.constraints.NotBlank;

public record EnrollRequest(@NotBlank String externalRef) {
}
