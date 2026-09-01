package studio.pixelforge.backend.portal;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(PortalProperties.class)
public class PortalConfig {

    // Отдельный RestClient только для запросов в кабинет: короткие таймауты,
    // чтобы медленный/недоступный портал не подвешивал фоновую задачу
    // синхронизации прогресса.
    @Bean
    RestClient portalRestClient(PortalProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);

        RestClient.Builder builder = RestClient.builder().requestFactory(factory);
        if (properties.baseUrl() != null && !properties.baseUrl().isBlank()) {
            builder.baseUrl(properties.baseUrl());
        }
        return builder.build();
    }
}
