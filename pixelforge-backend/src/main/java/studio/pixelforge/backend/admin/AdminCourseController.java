package studio.pixelforge.backend.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.course.*;

import java.util.List;

// Курсы и узлы дерева студии методиста — /api/admin/**, HMAC-guarded
// (AdminSignatureFilter). См. docs/integrations/pixelforge-studio-spec.md
// (learning-portal repo) §3.
@RestController
@RequestMapping("/api/admin")
public class AdminCourseController {

    private final CourseService courseService;
    private final CourseNodeService courseNodeService;

    public AdminCourseController(CourseService courseService, CourseNodeService courseNodeService) {
        this.courseService = courseService;
        this.courseNodeService = courseNodeService;
    }

    @GetMapping("/courses")
    public List<CourseResponse> list() {
        return courseService.list().stream().map(CourseResponse::from).toList();
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse create(@Valid @RequestBody CreateCourseRequest request) {
        return CourseResponse.from(courseService.create(request));
    }

    @GetMapping("/courses/{id}")
    public CourseResponse getById(@PathVariable Long id) {
        return CourseResponse.from(courseService.getById(id));
    }

    @PutMapping("/courses/{id}")
    public CourseResponse update(@PathVariable Long id, @RequestBody UpdateCourseRequest request) {
        return CourseResponse.from(courseService.update(id, request));
    }

    @DeleteMapping("/courses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseService.delete(id);
    }

    @PostMapping("/courses/{id}/archive")
    public CourseResponse archive(@PathVariable Long id) {
        return CourseResponse.from(courseService.archive(id));
    }

    @PostMapping("/courses/{id}/unarchive")
    public CourseResponse unarchive(@PathVariable Long id) {
        return CourseResponse.from(courseService.unarchive(id));
    }

    @GetMapping("/courses/{id}/tree")
    public CourseTreeResponse tree(@PathVariable Long id) {
        return courseService.tree(id);
    }

    @PostMapping("/courses/{courseId}/nodes")
    @ResponseStatus(HttpStatus.CREATED)
    public NodeResponse createNode(@PathVariable Long courseId, @Valid @RequestBody CreateNodeRequest request) {
        return NodeResponse.from(courseNodeService.create(courseId, request));
    }
}
