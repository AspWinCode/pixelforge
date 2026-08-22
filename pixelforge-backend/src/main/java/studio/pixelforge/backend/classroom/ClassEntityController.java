package studio.pixelforge.backend.classroom;

import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.user.UserRole;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ClassEntityController {

    private final ClassEntityService classEntityService;

    public ClassEntityController(ClassEntityService classEntityService) {
        this.classEntityService = classEntityService;
    }

    // Раньше был /teachers/classes — теперь методист и тренер используют
    // один и тот же эндпоинт, различие только в параметре role.
    @GetMapping("/staff/classes")
    public List<ClassEntityResponse> myClasses(@RequestParam Long userId, @RequestParam UserRole role) {
        return classEntityService.listForStaff(userId, role).stream()
            .map(ClassEntityResponse::from)
            .toList();
    }

    @GetMapping("/classes/{classId}/students")
    public List<StudentResponse> students(@PathVariable Long classId) {
        return classEntityService.listStudents(classId).stream()
            .map(StudentResponse::from)
            .toList();
    }
}
