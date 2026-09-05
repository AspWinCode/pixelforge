package studio.pixelforge.backend.assignment;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Автотесты и подсказки задачи (студия методиста, группа c). Чистые
// данные: PixelForge их не исполняет, показывает тренеру/ученику.
@Service
public class TaskExtrasService {

    private final AssignmentRepository assignmentRepository;
    private final TaskTestRepository taskTestRepository;
    private final TaskHintRepository taskHintRepository;

    public TaskExtrasService(AssignmentRepository assignmentRepository,
                             TaskTestRepository taskTestRepository,
                             TaskHintRepository taskHintRepository) {
        this.assignmentRepository = assignmentRepository;
        this.taskTestRepository = taskTestRepository;
        this.taskHintRepository = taskHintRepository;
    }

    private Assignment task(Long taskId) {
        return assignmentRepository.findById(taskId)
            .orElseThrow(() -> new EntityNotFoundException("Task not found: " + taskId));
    }

    // ---- tests ----

    @Transactional(readOnly = true)
    public List<TaskTest> listTests(Long taskId) {
        task(taskId);
        return taskTestRepository.findByAssignment_IdOrderByOrderIndexAsc(taskId);
    }

    @Transactional
    public TaskTest createTest(Long taskId, TaskTestRequest request) {
        TaskTest test = new TaskTest(task(taskId));
        test.setOrderIndex(taskTestRepository.findByAssignment_IdOrderByOrderIndexAsc(taskId).size());
        applyTest(test, request);
        return taskTestRepository.save(test);
    }

    @Transactional
    public TaskTest updateTest(Long id, TaskTestRequest request) {
        TaskTest test = taskTestRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Test not found: " + id));
        applyTest(test, request);
        return test;
    }

    @Transactional
    public void deleteTest(Long id) {
        if (!taskTestRepository.existsById(id)) {
            throw new EntityNotFoundException("Test not found: " + id);
        }
        taskTestRepository.deleteById(id);
    }

    private static void applyTest(TaskTest test, TaskTestRequest r) {
        if (r.testType() != null) {
            test.setTestType(r.testType());
        }
        if (r.inputData() != null) {
            test.setInputData(r.inputData());
        }
        if (r.expectedOutput() != null) {
            test.setExpectedOutput(r.expectedOutput());
        }
        if (r.checker() != null) {
            test.setChecker(r.checker());
        }
        if (r.weight() != null) {
            test.setWeight(r.weight());
        }
        if (r.orderIndex() != null) {
            test.setOrderIndex(r.orderIndex());
        }
    }

    // ---- hints ----

    @Transactional(readOnly = true)
    public List<TaskHint> listHints(Long taskId) {
        task(taskId);
        return taskHintRepository.findByAssignment_IdOrderByOrderIndexAsc(taskId);
    }

    @Transactional
    public TaskHint createHint(Long taskId, TaskHintRequest request) {
        if (request.content() == null || request.content().isBlank()) {
            throw new IllegalStateException("hint 'content' is required");
        }
        TaskHint hint = new TaskHint(task(taskId), request.content());
        hint.setOrderIndex(taskHintRepository.findByAssignment_IdOrderByOrderIndexAsc(taskId).size());
        applyHint(hint, request);
        return taskHintRepository.save(hint);
    }

    @Transactional
    public TaskHint updateHint(Long id, TaskHintRequest request) {
        TaskHint hint = taskHintRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Hint not found: " + id));
        if (request.content() != null) {
            hint.setContent(request.content());
        }
        applyHint(hint, request);
        return hint;
    }

    @Transactional
    public void deleteHint(Long id) {
        if (!taskHintRepository.existsById(id)) {
            throw new EntityNotFoundException("Hint not found: " + id);
        }
        taskHintRepository.deleteById(id);
    }

    private static void applyHint(TaskHint hint, TaskHintRequest r) {
        if (r.level() != null) {
            hint.setLevel(r.level());
        }
        if (r.unlockAttempts() != null) {
            hint.setUnlockAttempts(r.unlockAttempts());
        }
        if (r.coinCost() != null) {
            hint.setCoinCost(r.coinCost());
        }
        if (r.orderIndex() != null) {
            hint.setOrderIndex(r.orderIndex());
        }
    }
}
