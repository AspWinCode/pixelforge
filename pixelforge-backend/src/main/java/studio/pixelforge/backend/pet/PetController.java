package studio.pixelforge.backend.pet;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @GetMapping("/users/{userId}/pet")
    public PetStateResponse getPet(@PathVariable Long userId) {
        return PetStateResponse.from(petService.getOrCreate(userId));
    }

    @PostMapping("/users/{userId}/pet/feed")
    public PetStateResponse feed(@PathVariable Long userId) {
        return PetStateResponse.from(petService.feed(userId));
    }

    @PostMapping("/users/{userId}/pet/interact")
    public PetStateResponse interact(@PathVariable Long userId) {
        return PetStateResponse.from(petService.interact(userId));
    }

    @PostMapping("/users/{userId}/pet/rest")
    public PetStateResponse rest(@PathVariable Long userId) {
        return PetStateResponse.from(petService.rest(userId));
    }

    @PostMapping("/users/{userId}/pet/name")
    public PetStateResponse rename(@PathVariable Long userId, @RequestBody RenamePetRequest request) {
        return PetStateResponse.from(petService.rename(userId, request.name()));
    }

    @PostMapping("/internal/pet/trigger-decay")
    public void triggerDecay() {
        petService.triggerDecayManually();
    }
}
