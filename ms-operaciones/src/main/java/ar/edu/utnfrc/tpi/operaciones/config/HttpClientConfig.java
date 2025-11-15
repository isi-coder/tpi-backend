// ar.edu.utnfrc.tpi.operaciones.config.HttpClientConfig.java
package ar.edu.utnfrc.tpi.operaciones.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
}

