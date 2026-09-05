package studio.pixelforge.backend.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.lecture.*;

import java.util.List;

// Лекции (в UI — «брифинги») студии методиста. /api/admin/**, HMAC-guarded.
// Публичные read-эндпоинты для ученика остаются в LectureController.
@RestController
@RequestMapping("/api/admin")
public class AdminLectureController {

    private final LectureService lectureService;

    public AdminLectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @GetMapping("/lectures")
    public List<LectureResponse> list() {
        return lectureService.listAll().stream().map(LectureResponse::from).toList();
    }

    @PostMapping("/lectures")
    @ResponseStatus(HttpStatus.CREATED)
    public LectureResponse create(@Valid @RequestBody CreateLectureRequest request) {
        return LectureResponse.from(lectureService.create(request.title()));
    }

    @PutMapping("/lectures/{id}")
    public LectureResponse update(@PathVariable Long id, @RequestBody UpdateLectureRequest request) {
        return LectureResponse.from(lectureService.update(id, request.title()));
    }

    @DeleteMapping("/lectures/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        lectureService.delete(id);
    }

    @GetMapping("/lectures/{id}/cards")
    public List<LectureCardResponse> cards(@PathVariable Long id) {
        return lectureService.getCards(id).stream().map(LectureCardResponse::from).toList();
    }

    @PostMapping("/lectures/{id}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public LectureCardResponse addCard(@PathVariable Long id, @Valid @RequestBody AddCardRequest request) {
        return LectureCardResponse.from(lectureService.addCard(id, request.cardType(), request.content()));
    }

    @PostMapping("/lectures/{id}/cards/reorder")
    public void reorderCards(@PathVariable Long id, @Valid @RequestBody ReorderCardsRequest request) {
        lectureService.reorderCards(id, request.orderedIds());
    }

    @PutMapping("/lecture-cards/{id}")
    public LectureCardResponse updateCard(@PathVariable Long id, @RequestBody UpdateCardRequest request) {
        return LectureCardResponse.from(lectureService.updateCard(id, request.cardType(), request.content()));
    }

    @DeleteMapping("/lecture-cards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long id) {
        lectureService.deleteCard(id);
    }
}
