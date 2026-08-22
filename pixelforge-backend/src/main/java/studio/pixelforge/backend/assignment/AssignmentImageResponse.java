package studio.pixelforge.backend.assignment;

public record AssignmentImageResponse(Long id, String originalName, String url) {
    public static AssignmentImageResponse from(AssignmentImage img) {
        // URL строится через наш собственный эндпоинт /images/{id}, а не
        // напрямую в S3 — так мы не завязываемся на публичный доступ к бакету
        // и сможем позже добавить проверку прав перед отдачей файла.
        return new AssignmentImageResponse(img.getId(), img.getOriginalName(), "/api/assignments/images/" + img.getId());
    }
}
