package studio.pixelforge.backend.path;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PathController {

    private final PathService pathService;

    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @GetMapping("/api/classes/{classId}/path")
    public List<PathNode> getPath(@PathVariable Long classId, @RequestParam Long userId) {
        return pathService.buildPath(classId, userId);
    }
}
