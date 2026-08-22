package studio.pixelforge.backend.lecture;

import jakarta.validation.constraints.NotBlank;

public record CreateLectureRequest(@NotBlank String title) {}
