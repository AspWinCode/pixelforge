package studio.pixelforge.backend.npc;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class NpcController {

    private final NpcService npcService;

    public NpcController(NpcService npcService) {
        this.npcService = npcService;
    }

    @GetMapping("/users/{userId}/npc-messages")
    public List<NpcMessageResponse> list(@PathVariable Long userId) {
        return npcService.listForUser(userId).stream()
            .map(NpcMessageResponse::from)
            .toList();
    }

    @PostMapping("/npc-messages/{id}/read")
    public void markRead(@PathVariable Long id) {
        npcService.markAsRead(id);
    }
}
