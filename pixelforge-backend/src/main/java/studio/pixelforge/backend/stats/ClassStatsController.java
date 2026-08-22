package studio.pixelforge.backend.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClassStatsController {

    private final ClassStatsService classStatsService;

    public ClassStatsController(ClassStatsService classStatsService) {
        this.classStatsService = classStatsService;
    }

    @GetMapping("/api/classes/{classId}/stats")
    public ClassStatsResponse getStats(@PathVariable Long classId) {
        return classStatsService.buildStats(classId);
    }
}
