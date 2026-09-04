package studio.pixelforge.backend.lecture;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Ученические / общие read-эндпоинты лекций. Authoring (создание лекций и
// карточек) вынесен в studio.pixelforge.backend.admin.AdminLectureController
// под /api/admin/lectures.
@RestController
@RequestMapping("/api/lectures")
public class LectureController {

    private final LectureService lectureService;

    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @GetMapping
    public List<LectureResponse> listAll() {
        return lectureService.listAll().stream().map(LectureResponse::from).toList();
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
