package studio.pixelforge.backend.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import studio.pixelforge.backend.assignment.AssignmentImage;
import studio.pixelforge.backend.assignment.AssignmentImageResponse;
import studio.pixelforge.backend.assignment.AssignmentResponse;
import studio.pixelforge.backend.assignment.AssignmentService;
import studio.pixelforge.backend.assignment.CreateAssignmentRequest;

import java.io.IOException;
import java.io.UncheckedIOException;

// Authoring-часть заданий. Раньше жила в AssignmentController под /api,
// перенесена под /api/admin/** и закрыта AdminSignatureFilter (см. §1
// спеки интеграции PixelForge Studio). Ученические/общие GET-эндпоинты
// остались в AssignmentController.
@RestController
@RequestMapping("/api/admin")
public class AdminAssignmentController {

    private final AssignmentService assignmentService;

    public AdminAssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(@Valid @RequestBody CreateAssignmentRequest request) {
        return AssignmentResponse.from(assignmentService.create(request));
    }

    @PostMapping("/assignments/{id}/publish")
    public AssignmentResponse publish(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.publish(id));
    }

    @PostMapping(value = "/assignments/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
}
