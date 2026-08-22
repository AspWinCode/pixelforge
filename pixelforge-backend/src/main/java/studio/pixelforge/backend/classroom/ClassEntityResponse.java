package studio.pixelforge.backend.classroom;

public record ClassEntityResponse(Long id, String name) {
    public static ClassEntityResponse from(ClassEntity c) {
        return new ClassEntityResponse(c.getId(), c.getName());
    }
}
