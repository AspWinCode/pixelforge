package studio.pixelforge.backend.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.course.CourseNodeService;
import studio.pixelforge.backend.course.MoveNodeRequest;
import studio.pixelforge.backend.course.NodeResponse;
import studio.pixelforge.backend.course.ReorderNodesRequest;
import studio.pixelforge.backend.course.UpdateNodeRequest;

@RestController
@RequestMapping("/api/admin/nodes")
public class AdminCourseNodeController {

    private final CourseNodeService courseNodeService;

    public AdminCourseNodeController(CourseNodeService courseNodeService) {
        this.courseNodeService = courseNodeService;
    }

    @PutMapping("/{id}")
    public NodeResponse update(@PathVariable Long id, @RequestBody UpdateNodeRequest request) {
        return NodeResponse.from(courseNodeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        courseNodeService.delete(id);
    }

    @PostMapping("/{id}/move")
    public NodeResponse move(@PathVariable Long id, @RequestBody MoveNodeRequest request) {
        return NodeResponse.from(courseNodeService.move(id, request));
    }

    @PostMapping("/reorder")
    public void reorder(@Valid @RequestBody ReorderNodesRequest request) {
        courseNodeService.reorder(request);
    }
}
