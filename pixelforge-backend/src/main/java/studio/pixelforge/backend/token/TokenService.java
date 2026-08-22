package studio.pixelforge.backend.token;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.npc.NpcService;
import studio.pixelforge.backend.pet.PetService;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

import java.util.Map;

@Service
public class TokenService {

    private final TokenEventRepository tokenEventRepository;
    private final UserRepository userRepository;
    private final PetService petService;
    private final NpcService npcService;

    public TokenService(TokenEventRepository tokenEventRepository,
                         UserRepository userRepository,
                         PetService petService,
                         NpcService npcService) {
        this.tokenEventRepository = tokenEventRepository;
        this.userRepository = userRepository;
        this.petService = petService;
        this.npcService = npcService;
    }

    @Transactional
    public TokenEvent award(Long userId, Integer amount, String reason, Map<String, Object> meta) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        // Баланс ДО начисления — нужен, чтобы сравнить ранг до и после
        // и понять, произошло ли повышение именно на этом событии.
        long balanceBefore = tokenEventRepository.sumAmountByUserId(userId);

        TokenEvent event = tokenEventRepository.save(
            new TokenEvent(user, user.getOrganization(), amount, reason, meta)
        );

        long balanceAfter = balanceBefore + amount;
        Rank rankBefore = Rank.fromBalance(balanceBefore);
        Rank rankAfter = Rank.fromBalance(balanceAfter);

        // Уровень питомца всегда синхронизируем с текущим рангом (метод сам
        // защищён от понижения), а сообщение от NPC — только если ранг
        // реально изменился, чтобы не спамить одинаковым поздравлением.
        petService.setLevelFromRank(userId, rankAfter.ordinal() + 1);
        if (rankAfter != rankBefore) {
            npcService.onRankUp(userId, rankAfter);
        }

        return event;
    }

    @Transactional(readOnly = true)
    public Long balance(Long userId) {
        return tokenEventRepository.sumAmountByUserId(userId);
    }

    @Transactional(readOnly = true)
    public Rank rank(Long userId) {
        return Rank.fromBalance(balance(userId));
    }
}
