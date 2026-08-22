package studio.pixelforge.backend.user;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import studio.pixelforge.backend.npc.NpcService;
import studio.pixelforge.backend.token.Rank;
import studio.pixelforge.backend.token.TokenService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    // Разовый бонус за прохождение онбординга — стартовая "зарплата".
    private static final int ONBOARDING_BONUS_TOKENS = 15;

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final NpcService npcService;

    public UserController(UserRepository userRepository, TokenService tokenService, NpcService npcService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.npcService = npcService;
    }

    @GetMapping("/users/{userId}")
    public UserResponse get(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return UserResponse.from(user);
    }

    // Идемпотентно: повторный вызов (например, из-за ретрая на фронте) не
    // начисляет бонус дважды — награда выдаётся только при первом переходе
    // онбординга из false в true.
    @Transactional
    @PostMapping("/users/{userId}/onboarding/complete")
    public OnboardingCompleteResponse completeOnboarding(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        if (!user.getOnboardingCompleted()) {
            user.setOnboardingCompleted(true);
            tokenService.award(userId, ONBOARDING_BONUS_TOKENS, "onboarding_completed", Map.of());
            npcService.onTokensAwarded(userId, ONBOARDING_BONUS_TOKENS);
        }

        long balance = tokenService.balance(userId);
        Rank rank = Rank.fromBalance(balance);
        return new OnboardingCompleteResponse(balance, rank.getDisplayName());
    }
}
