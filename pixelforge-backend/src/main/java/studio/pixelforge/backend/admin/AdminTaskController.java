package studio.pixelforge.backend.admin;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import studio.pixelforge.backend.assignment.AssignmentImage;
import studio.pixelforge.backend.assignment.AssignmentImageResponse;
import studio.pixelforge.backend.assignment.AssignmentResponse;
import studio.pixelforge.backend.assignment.AssignmentService;
import studio.pixelforge.backend.assignment.UpdateTaskRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

// Задачи студии методиста (в коде — Assignment). /api/admin/**, HMAC-guarded.
// Байты картинок отдаются публично через AssignmentController
// (GET /api/assignments/images/{id}) — как и раньше.
@RestController
@RequestMapping("/api/admin/tasks")
public class AdminTaskController {

    private final AssignmentService assignmentService;

    public AdminTaskController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/{id}")
    public AssignmentResponse getById(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.getById(id));
    }

    @PutMapping("/{id}")
    public AssignmentResponse update(@PathVariable Long id, @RequestBody UpdateTaskRequest request) {
        return AssignmentResponse.from(assignmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        assignmentService.delete(id);
    }

    @PostMapping("/{id}/publish")
    public AssignmentResponse publish(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.publish(id));
    }

    @PostMapping("/{id}/unpublish")
    public AssignmentResponse unpublish(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.unpublish(id));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AssignmentImageResponse uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        try {
            AssignmentImage image = assignmentService.addImage(
                id, file.getOriginalFilename(), file.getBytes(), file.getContentType()
            );
            return AssignmentImageResponse.from(image);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @GetMapping("/{id}/images")
    public List<AssignmentImageResponse> listImages(@PathVariable Long id) {
        return assignmentService.listImages(id).stream()
            .map(AssignmentImageResponse::from)
            .toList();
    }
}
