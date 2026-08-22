package studio.pixelforge.backend.pet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetStateRepository extends JpaRepository<PetState, Long> {

    Optional<PetState> findByUser_Id(Long userId);

    // Нужен для ежедневного угасания разом у всех питомцев, а не по одному.
    List<PetState> findAll();
}
