package studio.pixelforge.backend.npc;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NpcMessageRepository extends JpaRepository<NpcMessage, Long> {

    List<NpcMessage> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndIsReadFalse(Long userId);
}
