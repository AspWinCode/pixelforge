package studio.pixelforge.backend.storage;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(S3Properties.class)
public class S3Config {

    @Bean
    public S3Client s3Client(S3Properties props) {
        return S3Client.builder()
            .endpointOverride(URI.create(props.endpoint()))
            .region(Region.of(props.region()))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.accessKey(), props.secretKey())
            ))
            .serviceConfiguration(S3Configuration.builder()
                // MinIO требует path-style адресацию (http://host/bucket/key),
                // а не virtual-hosted-style (http://bucket.host/key) как настоящий AWS S3.
                // Когда переедем на реальный AWS — эту строчку нужно будет убрать.
                .pathStyleAccessEnabled(true)
                .chunkedEncodingEnabled(false)
                .build())
            // Для PutObject S3-модуль SDK всегда сам добавляет заголовок
            // "Expect: 100-continue" на уровне абстрактного запроса (зашито
            // в StreamingRequestInterceptor), а Apache HTTP-клиент независимо
            // от этого решает то же самое на уровне сокета через свой
            // RequestConfig. MinIO не отвечает на continue вовремя — из-за
            // этого каждая загрузка висела ровно 3 секунды, прежде чем
            // клиент всё равно отправлял тело. Отключаем оба источника.
            .overrideConfiguration(c -> c.addExecutionInterceptor(new DisableExpectContinueInterceptor()))
            .httpClientBuilder(ApacheHttpClient.builder().expectContinueEnabled(false))
            .build();
    }
}
