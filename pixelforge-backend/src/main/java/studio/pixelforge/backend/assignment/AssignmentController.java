package studio.pixelforge.backend.assignment;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Ученические / общие read-эндпоинты заданий. Authoring (создание задач,
// публикация, загрузка картинок) вынесен в
// studio.pixelforge.backend.admin.AdminTaskController под /api/admin/**.
@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/assignments/{id}")
    public AssignmentResponse getById(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.getById(id));
    }

    @GetMapping("/classes/{classId}/assignments")
    public List<AssignmentResponse> listPublished(@PathVariable Long classId) {
        return assignmentService.listPublishedForClass(classId).stream()
            .map(AssignmentResponse::from)
            .toList();
    }

    @GetMapping("/classes/{classId}/assignments/all")
    public List<AssignmentResponse> listAll(@PathVariable Long classId) {
        return assignmentService.listAllForClass(classId).stream()
            .map(AssignmentResponse::from)
            .toList();
    }

    @GetMapping("/assignments/{id}/images")
    public List<AssignmentImageResponse> listImages(@PathVariable Long id) {
        return assignmentService.listImages(id).stream()
            .map(AssignmentImageResponse::from)
            .toList();
    }

    @GetMapping("/assignments/images/{imageId}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long imageId) {
        AssignmentImage image = assignmentService.getImage(imageId);
        byte[] bytes = assignmentService.getImageBytes(image.getS3Key());
        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(image.getContentType()))
            .body(bytes);
    }
}
