package studio.pixelforge.backend.pet;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import studio.pixelforge.backend.user.User;
import studio.pixelforge.backend.user.UserRepository;

import java.time.Instant;

@Service
public class PetService {

    private static final int DAILY_DECAY = 15;
    private static final int FEED_AMOUNT = 25;
    private static final int INTERACT_AMOUNT = 20;
    private static final int REST_AMOUNT = 30;

    private final PetStateRepository petStateRepository;
    private final UserRepository userRepository;

    public PetService(PetStateRepository petStateRepository, UserRepository userRepository) {
        this.petStateRepository = petStateRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public PetState getOrCreate(Long userId) {
        return petStateRepository.findByUser_Id(userId)
            .orElseGet(() -> {
                User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
                return petStateRepository.save(new PetState(user));
            });
    }

    @Transactional
    public PetState feed(Long userId) {
        PetState pet = getOrCreate(userId);
        pet.setHunger(PetState.clamp(pet.getHunger() + FEED_AMOUNT));
        pet.setUpdatedAt(Instant.now());
        return pet;
    }

    @Transactional
    public PetState interact(Long userId) {
        PetState pet = getOrCreate(userId);
        pet.setMood(PetState.clamp(pet.getMood() + INTERACT_AMOUNT));
        pet.setUpdatedAt(Instant.now());
        return pet;
    }

    // Третье действие — закрывает пробел: раньше energy могла только
    // снижаться (угасание), но никогда не восстанавливалась.
    @Transactional
    public PetState rest(Long userId) {
        PetState pet = getOrCreate(userId);
        pet.setEnergy(PetState.clamp(pet.getEnergy() + REST_AMOUNT));
        pet.setUpdatedAt(Instant.now());
        return pet;
    }

    @Transactional
    public PetState rename(Long userId, String name) {
        PetState pet = getOrCreate(userId);
        String trimmed = name == null ? "" : name.trim();
        pet.setName(trimmed.isEmpty() ? null : trimmed.substring(0, Math.min(32, trimmed.length())));
        pet.setUpdatedAt(Instant.now());
        return pet;
    }

    @Transactional
    public void setLevelFromRank(Long userId, int rankBasedLevel) {
        PetState pet = getOrCreate(userId);
        if (rankBasedLevel > pet.getLevel()) {
            pet.setLevel(rankBasedLevel);
            pet.setUpdatedAt(Instant.now());
        }
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void applyDailyDecayToAll() {
        for (PetState pet : petStateRepository.findAll()) {
            pet.setHunger(PetState.clamp(pet.getHunger() - DAILY_DECAY));
            pet.setMood(PetState.clamp(pet.getMood() - DAILY_DECAY));
            pet.setEnergy(PetState.clamp(pet.getEnergy() - DAILY_DECAY));
            pet.setUpdatedAt(Instant.now());
        }
    }

    @Transactional
    public void triggerDecayManually() {
        applyDailyDecayToAll();
    }
}
