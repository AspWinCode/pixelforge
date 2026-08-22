package studio.pixelforge.backend.pet;

public record PetStateResponse(Long userId, int hunger, int mood, int energy, int level, String name) {
    public static PetStateResponse from(PetState pet) {
        return new PetStateResponse(
            pet.getUser().getId(), pet.getHunger(), pet.getMood(), pet.getEnergy(), pet.getLevel(), pet.getName()
        );
    }
}
