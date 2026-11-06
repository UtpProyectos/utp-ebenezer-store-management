package pe.edu.utp.ebenezer.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * HTTP clients for external services. Only the AI providers use one for now.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class RestClientConfig {

    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    @Bean
    public RestClient aiRestClient(AiProperties aiProperties) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(CONNECT_TIMEOUT).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(aiProperties.timeoutSeconds()));
        return RestClient.builder().requestFactory(requestFactory).build();
    }
}
