package studio.pixelforge.backend.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.course.CreateNodeTaskRequest;
import studio.pixelforge.backend.course.NodeTaskResponse;
import studio.pixelforge.backend.course.NodeTaskService;
import studio.pixelforge.backend.course.ReorderNodeTasksRequest;

// Привязка задач к узлам дерева курса. /api/admin/**, HMAC-guarded.
@RestController
@RequestMapping("/api/admin/nodes/{nodeId}/tasks")
public class AdminNodeTaskController {

    private final NodeTaskService nodeTaskService;

    public AdminNodeTaskController(NodeTaskService nodeTaskService) {
        this.nodeTaskService = nodeTaskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NodeTaskResponse attach(@PathVariable Long nodeId, @Valid @RequestBody CreateNodeTaskRequest request) {
        return NodeTaskResponse.from(nodeTaskService.attach(nodeId, request));
    }

    @DeleteMapping("/{nodeTaskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detach(@PathVariable Long nodeId, @PathVariable Long nodeTaskId) {
        nodeTaskService.detach(nodeId, nodeTaskId);
    }

    @PostMapping("/reorder")
    public void reorder(@PathVariable Long nodeId, @Valid @RequestBody ReorderNodeTasksRequest request) {
        nodeTaskService.reorder(nodeId, request);
    }
}
