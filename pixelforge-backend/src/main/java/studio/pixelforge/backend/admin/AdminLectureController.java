package studio.pixelforge.backend.admin;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.lecture.AddCardRequest;
import studio.pixelforge.backend.lecture.CreateLectureRequest;
import studio.pixelforge.backend.lecture.LectureCardResponse;
import studio.pixelforge.backend.lecture.LectureResponse;
import studio.pixelforge.backend.lecture.LectureService;

// Authoring-часть лекций. Раньше жила в LectureController под /api/lectures,
// перенесена под /api/admin/** и закрыта AdminSignatureFilter. Read-эндпоинты
// (список, карточки, прогресс, отметка "пройдено") остались в LectureController.
@RestController
@RequestMapping("/api/admin/lectures")
public class AdminLectureController {

    private final LectureService lectureService;

    public AdminLectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @PostMapping
    public LectureResponse create(@Valid @RequestBody CreateLectureRequest request) {
        return LectureResponse.from(lectureService.create(request.title()));
    }

    @PostMapping("/{id}/cards")
    public LectureCardResponse addCard(@PathVariable Long id, @Valid @RequestBody AddCardRequest request) {
        return LectureCardResponse.from(lectureService.addCard(id, request.cardType(), request.content()));
    }
}
