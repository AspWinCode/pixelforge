package studio.pixelforge.backend.npc;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.token.Rank;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

import java.util.List;
import java.util.Random;

@Service
public class NpcService {

    private static final List<String> SUBMIT_TEMPLATES = List.of(
        "Отличная работа над «%s»! Ждём проверки от учителя.",
        "«%s» сдано! Ты молодец, что довёл дело до конца.",
        "Задание «%s» улетело на проверку. Гордись собой!"
    );

    private static final List<String> TOKENS_TEMPLATES = List.of(
        "Учитель проверил твою работу и начислил %d токенов! Так держать.",
        "+%d токенов на счету! Ты заслужил это своей работой.",
        "Отличный результат — %d токенов только что упали на баланс!"
    );

    private static final List<String> REMINDER_TEMPLATES = List.of(
        "Слушай, а ты случайно не забыл сдать «%s»? Работа сохранена, осталось только нажать «Сдать»!",
        "Я тут заметил, что «%s» так и осталось несданным — может, доделаешь, когда будет время?",
        "«%s» ждёт тебя! Прогресс сохранён, просто не забудь довести дело до конца."
    );

    private final NpcMessageRepository npcMessageRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public NpcService(NpcMessageRepository npcMessageRepository, UserRepository userRepository) {
        this.npcMessageRepository = npcMessageRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void onAssignmentSubmitted(Long userId, String assignmentTitle) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        String template = SUBMIT_TEMPLATES.get(random.nextInt(SUBMIT_TEMPLATES.size()));
        npcMessageRepository.save(new NpcMessage(user, NpcCharacter.MENTOR, template.formatted(assignmentTitle)));
    }

    @Transactional
    public void onTokensAwarded(Long userId, int amount) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        String template = TOKENS_TEMPLATES.get(random.nextInt(TOKENS_TEMPLATES.size()));
        npcMessageRepository.save(new NpcMessage(user, NpcCharacter.CEO, template.formatted(amount)));
    }

    @Transactional
    public void onRankUp(Long userId, Rank newRank) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        String message = "🎉 Поздравляем! Твой новый ранг — " + newRank.getDisplayName() + "! Питомец подрос вместе с тобой.";
        npcMessageRepository.save(new NpcMessage(user, NpcCharacter.CEO, message));
    }

    // Напоминание про забытую, но не сданную работу — срабатывает,
    // когда ученик открыл 5+ других заданий, не закончив эту.
    @Transactional
    public void onForgottenSubmission(Long userId, String assignmentTitle) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        String template = REMINDER_TEMPLATES.get(random.nextInt(REMINDER_TEMPLATES.size()));
        npcMessageRepository.save(new NpcMessage(user, NpcCharacter.MENTOR, template.formatted(assignmentTitle)));
    }

    @Transactional(readOnly = true)
    public List<NpcMessage> listForUser(Long userId) {
        return npcMessageRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(Long messageId) {
        NpcMessage message = npcMessageRepository.findById(messageId)
            .orElseThrow(() -> new EntityNotFoundException("Message not found: " + messageId));
        message.setRead(true);
    }
}
