package studio.pixelforge.backend.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Читает секцию pixelforge.s3.* из application.yml.
// Record + @ConfigurationProperties — современный способ в Spring Boot 3,
// не нужен отдельный @ConstructorBinding.
@ConfigurationProperties(prefix = "pixelforge.s3")
public record S3Properties(
    String endpoint,
    String bucket,
    String accessKey,
    String secretKey,
    String region
) {}
