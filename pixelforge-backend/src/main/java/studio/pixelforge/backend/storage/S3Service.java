package studio.pixelforge.backend.storage;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Service
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties properties;

    public S3Service(S3Client s3Client, S3Properties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public void uploadXml(String key, String xmlContent) {
        uploadBytes(key, xmlContent.getBytes(StandardCharsets.UTF_8), "application/xml");
    }

    public String downloadXml(String key) {
        return new String(downloadBytes(key), StandardCharsets.UTF_8);
    }

    // Универсальный метод для любых бинарных файлов — картинок задания,
    // а в будущем, возможно, других вложений. XML-методы выше просто
    // переиспользуют его с конкретной кодировкой/типом.
    public void uploadBytes(String key, byte[] content, String contentType) {
        s3Client.putObject(
            PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(key)
                .contentType(contentType)
                .build(),
            RequestBody.fromBytes(content)
        );
    }

    public byte[] downloadBytes(String key) {
        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(
                GetObjectRequest.builder()
                    .bucket(properties.bucket())
                    .key(key)
                    .build()
        )) {
            return response.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
