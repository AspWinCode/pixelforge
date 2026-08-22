package studio.pixelforge.backend.classroom;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import studio.pixelforge.backend.user.UserRole;

import java.util.List;

public interface ClassMemberRepository extends JpaRepository<ClassMember, ClassMemberId> {

    List<ClassMember> findByClassEntity_Id(Long classId);

    List<ClassMember> findByUser_Id(Long userId);

    // Роль теперь параметр, а не захардкожена — используется и методистом,
    // и тренером с одним и тем же запросом.
    @Query("SELECT cm.classEntity FROM ClassMember cm WHERE cm.user.id = :userId AND cm.user.role = :role")
    List<ClassEntity> findClassesByStaffId(@Param("userId") Long userId, @Param("role") UserRole role);

    // Список ВСЕХ учеников класса — нужен тренеру для сводной таблицы.
    @Query("SELECT cm.user FROM ClassMember cm WHERE cm.classEntity.id = :classId AND cm.user.role = 'STUDENT'")
    List<studio.pixelforge.backend.user.User> findStudentsByClassId(@Param("classId") Long classId);
}
