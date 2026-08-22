package studio.pixelforge.backend.classroom;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRole;

import java.util.List;

@Service
public class ClassEntityService {

    private final ClassMemberRepository classMemberRepository;

    public ClassEntityService(ClassMemberRepository classMemberRepository) {
        this.classMemberRepository = classMemberRepository;
    }

    @Transactional(readOnly = true)
    public List<ClassEntity> listForStaff(Long userId, UserRole role) {
        return classMemberRepository.findClassesByStaffId(userId, role);
    }

    @Transactional(readOnly = true)
    public List<User> listStudents(Long classId) {
        return classMemberRepository.findStudentsByClassId(classId);
    }
}
