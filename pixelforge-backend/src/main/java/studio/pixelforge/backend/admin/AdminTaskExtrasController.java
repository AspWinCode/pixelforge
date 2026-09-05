package studio.pixelforge.backend.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.assignment.*;

import java.util.List;

// Автотесты и подсказки задач студии. /api/admin/**, HMAC-guarded.
// GET-списки добавлены сверх спеки §3 — портальному UI нужно показывать
// уже заведённые тесты/подсказки.
@RestController
@RequestMapping("/api/admin")
public class AdminTaskExtrasController {

    private final TaskExtrasService service;

    public AdminTaskExtrasController(TaskExtrasService service) {
        this.service = service;
    }

    // ---- tests ----

    @GetMapping("/tasks/{taskId}/tests")
    public List<TaskTestResponse> listTests(@PathVariable Long taskId) {
        return service.listTests(taskId).stream().map(TaskTestResponse::from).toList();
    }

    @PostMapping("/tasks/{taskId}/tests")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskTestResponse createTest(@PathVariable Long taskId, @RequestBody TaskTestRequest request) {
        return TaskTestResponse.from(service.createTest(taskId, request));
    }

    @PutMapping("/tests/{id}")
    public TaskTestResponse updateTest(@PathVariable Long id, @RequestBody TaskTestRequest request) {
        return TaskTestResponse.from(service.updateTest(id, request));
    }

    @DeleteMapping("/tests/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTest(@PathVariable Long id) {
        service.deleteTest(id);
    }

    // ---- hints ----

    @GetMapping("/tasks/{taskId}/hints")
    public List<TaskHintResponse> listHints(@PathVariable Long taskId) {
        return service.listHints(taskId).stream().map(TaskHintResponse::from).toList();
    }

    @PostMapping("/tasks/{taskId}/hints")
    @ResponseStatus(HttpStatus.CREATED)
    public TaskHintResponse createHint(@PathVariable Long taskId, @RequestBody TaskHintRequest request) {
        return TaskHintResponse.from(service.createHint(taskId, request));
    }

    @PutMapping("/hints/{id}")
    public TaskHintResponse updateHint(@PathVariable Long id, @RequestBody TaskHintRequest request) {
        return TaskHintResponse.from(service.updateHint(id, request));
    }

    @DeleteMapping("/hints/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHint(@PathVariable Long id) {
        service.deleteHint(id);
    }
}
