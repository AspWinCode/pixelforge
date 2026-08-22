package studio.pixelforge.backend.lecture;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/lectures")
public class LectureController {

    private final LectureService lectureService;

    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @PostMapping
    public LectureResponse create(@Valid @RequestBody CreateLectureRequest request) {
        return LectureResponse.from(lectureService.create(request.title()));
    }

    @GetMapping
    public List<LectureResponse> listAll() {
        return lectureService.listAll().stream().map(LectureResponse::from).toList();
    }

    @PostMapping("/{id}/cards")
    public LectureCardResponse addCard(@PathVariable Long id, @Valid @RequestBody AddCardRequest request) {
        return LectureCardResponse.from(lectureService.addCard(id, request.cardType(), request.content()));
    }

    @GetMapping("/{id}/cards")
    public List<LectureCardResponse> getCards(@PathVariable Long id) {
        return lectureService.getCards(id).stream().map(LectureCardResponse::from).toList();
    }

    @GetMapping("/{id}/completion")
    public Map<String, Boolean> completion(@PathVariable Long id, @RequestParam Long userId) {
        return Map.of("completed", lectureService.isCompleted(id, userId));
    }

    @PostMapping("/{id}/complete")
    public void complete(@PathVariable Long id, @RequestParam Long userId) {
        lectureService.markCompleted(id, userId);
    }
}
