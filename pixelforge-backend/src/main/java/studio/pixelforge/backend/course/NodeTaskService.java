package studio.pixelforge.backend.course;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.assignment.Assignment;
import studio.pixelforge.backend.assignment.AssignmentService;

import java.util.List;

@Service
public class NodeTaskService {

    private final CourseNodeRepository courseNodeRepository;
    private final NodeTaskRepository nodeTaskRepository;
    private final AssignmentService assignmentService;

    public NodeTaskService(CourseNodeRepository courseNodeRepository,
                            NodeTaskRepository nodeTaskRepository,
                            AssignmentService assignmentService) {
        this.courseNodeRepository = courseNodeRepository;
        this.nodeTaskRepository = nodeTaskRepository;
        this.assignmentService = assignmentService;
    }

    @Transactional
    public NodeTask attach(Long nodeId, CreateNodeTaskRequest request) {
        CourseNode node = courseNodeRepository.findById(nodeId)
            .orElseThrow(() -> new EntityNotFoundException("Course node not found: " + nodeId));

        Assignment assignment;
        if (request.createNew()) {
            if (request.title() == null || request.title().isBlank() || request.tool() == null) {
                throw new IllegalStateException("createNew requires 'title' and 'tool'");
            }
            assignment = assignmentService.createTemplate(request.title(), request.tool());
        } else {
            if (request.assignmentId() == null) {
                throw new IllegalStateException("linking an existing task requires 'assignmentId'");
            }
            assignment = assignmentService.getById(request.assignmentId());
        }

        nodeTaskRepository.findByNode_IdAndAssignment_Id(nodeId, assignment.getId()).ifPresent(nt -> {
            throw new IllegalStateException("Task is already attached to this node");
        });

        NodeTask nodeTask = new NodeTask(node, assignment);
        nodeTask.setSortOrder(nodeTaskRepository.findByNode_IdOrderBySortOrderAsc(nodeId).size());
        if (request.isRequired() != null) {
            nodeTask.setRequired(request.isRequired());
        }
        return nodeTaskRepository.save(nodeTask);
    }

    // Отвязать задачу от узла — сама задача (assignment) не удаляется.
    @Transactional
    public void detach(Long nodeId, Long nodeTaskId) {
        NodeTask nodeTask = nodeTaskRepository.findById(nodeTaskId)
            .orElseThrow(() -> new EntityNotFoundException("node_task not found: " + nodeTaskId));
        if (!nodeTask.getNode().getId().equals(nodeId)) {
            throw new IllegalStateException("node_task " + nodeTaskId + " does not belong to node " + nodeId);
        }
        nodeTaskRepository.delete(nodeTask);
    }

    @Transactional
    public void reorder(Long nodeId, ReorderNodeTasksRequest request) {
        List<NodeTask> items = nodeTaskRepository.findAllById(request.orderedIds());
        if (items.size() != request.orderedIds().size()) {
            throw new EntityNotFoundException("One or more node_task ids do not exist");
        }
        for (NodeTask nt : items) {
            if (!nt.getNode().getId().equals(nodeId)) {
                throw new IllegalStateException("node_task " + nt.getId() + " does not belong to node " + nodeId);
            }
        }
        for (int i = 0; i < request.orderedIds().size(); i++) {
            Long id = request.orderedIds().get(i);
            int position = i;
            items.stream().filter(nt -> nt.getId().equals(id)).findFirst()
                .ifPresent(nt -> nt.setSortOrder(position));
        }
    }
}
