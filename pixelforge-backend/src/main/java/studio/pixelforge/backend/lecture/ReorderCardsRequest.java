package studio.pixelforge.backend.lecture;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record ReorderCardsRequest(@NotEmpty List<Long> orderedIds) {
}
