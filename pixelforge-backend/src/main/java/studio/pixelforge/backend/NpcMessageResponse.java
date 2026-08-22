package studio.pixelforge.backend.npc;

import java.time.Instant;

public record NpcMessageResponse(Long id, NpcCharacter character, String message, boolean isRead, Instant createdAt) {
    public static NpcMessageResponse from(NpcMessage m) {
        return new NpcMessageResponse(m.getId(), m.getCharacter(), m.getMessage(), m.isRead(), m.getCreatedAt());
    }
}
