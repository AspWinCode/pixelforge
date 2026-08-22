package studio.pixelforge.backend.classroom;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassEntityRepository extends JpaRepository<ClassEntity, Long> {

    List<ClassEntity> findByOrganization_Id(Long organizationId);

    Optional<ClassEntity> findByOrganization_IdAndLmsClassId(Long organizationId, String lmsClassId);
}
