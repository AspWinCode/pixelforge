package studio.pixelforge.backend.submission;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/assignments/{assignmentId}/submissions/start")
    public SubmissionResponse start(@PathVariable Long assignmentId, @RequestParam Long userId) {
        return SubmissionResponse.from(submissionService.startOrGet(assignmentId, userId));
    }

    @GetMapping("/assignments/{assignmentId}/submissions/project")
    public ProjectXmlResponse getProject(@PathVariable Long assignmentId, @RequestParam Long userId) {
        String xml = submissionService.getSavedProjectXml(assignmentId, userId).orElse(null);
        return new ProjectXmlResponse(xml);
    }

    @PostMapping("/assignments/{assignmentId}/submissions/save")
    public SubmissionResponse save(@PathVariable Long assignmentId,
                                    @RequestParam Long userId,
                                    @RequestBody SaveProjectRequest request) {
        return SubmissionResponse.from(submissionService.saveProject(assignmentId, userId, request.xml()));
    }

    @PostMapping("/assignments/{assignmentId}/submissions/submit")
    public SubmissionResponse submit(@PathVariable Long assignmentId, @RequestParam Long userId) {
        return SubmissionResponse.from(submissionService.submit(assignmentId, userId));
    }

    @PostMapping("/assignments/{assignmentId}/submissions/review")
    public SubmissionResponse review(@PathVariable Long assignmentId, @RequestParam Long userId) {
        return SubmissionResponse.from(submissionService.review(assignmentId, userId));
    }

    // Новый эндпоинт: список всех сдач по заданию — то, чего не хватало
    // учителю на практике, чтобы видеть, кого вообще нужно проверять.
    @GetMapping("/assignments/{assignmentId}/submissions")
    public List<SubmissionResponse> listByAssignment(@PathVariable Long assignmentId) {
        return submissionService.listByAssignment(assignmentId).stream()
            .map(SubmissionResponse::from)
            .toList();
    }
}
