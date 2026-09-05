package studio.pixelforge.backend.admin;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import studio.pixelforge.backend.classroom.ClassEntityRepository;
import studio.pixelforge.backend.classroom.ClassEntityResponse;
import studio.pixelforge.backend.classroom.ClassEntityService;
import studio.pixelforge.backend.classroom.StudentResponse;

import java.util.List;

// Классы — только чтение, для выбора при привязке задачи-шаблона к классу
// (PUT /api/admin/tasks/{id} с classId). Заводятся классы через
// /api/lms/sync/class, CRUD в студии не нужен. /api/admin/**, HMAC-guarded.
@RestController
@RequestMapping("/api/admin/classes")
public class AdminClassController {

    private static final Long ORG_ID = 1L;

    private final ClassEntityRepository classEntityRepository;
    private final ClassEntityService classEntityService;

    public AdminClassController(ClassEntityRepository classEntityRepository,
                                ClassEntityService classEntityService) {
        this.classEntityRepository = classEntityRepository;
        this.classEntityService = classEntityService;
    }

    @GetMapping
    public List<ClassEntityResponse> list() {
        return classEntityRepository.findByOrganization_Id(ORG_ID).stream()
            .map(ClassEntityResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public ClassEntityResponse getById(@PathVariable Long id) {
        return classEntityRepository.findById(id)
            .filter(c -> c.getOrganization().getId().equals(ORG_ID))
            .map(ClassEntityResponse::from)
            .orElseThrow(() -> new EntityNotFoundException("Class not found: " + id));
    }

    @GetMapping("/{id}/students")
    public List<StudentResponse> students(@PathVariable Long id) {
        return classEntityService.listStudents(id).stream()
            .map(StudentResponse::from)
            .toList();
    }
}
