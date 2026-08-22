package studio.pixelforge.backend.assignment;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping("/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentResponse create(@Valid @RequestBody CreateAssignmentRequest request) {
        return AssignmentResponse.from(assignmentService.create(request));
    }

    @GetMapping("/assignments/{id}")
    public AssignmentResponse getById(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.getById(id));
    }

    @PostMapping("/assignments/{id}/publish")
    public AssignmentResponse publish(@PathVariable Long id) {
        return AssignmentResponse.from(assignmentService.publish(id));
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
