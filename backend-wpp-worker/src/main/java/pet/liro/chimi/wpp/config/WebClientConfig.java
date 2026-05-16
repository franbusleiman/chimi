package pet.liro.chimi.wpp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean(name = "graphClient")
    public WebClient graphClient(WppProperties props) {
        return WebClient.builder()
                .baseUrl(props.graphBaseUrl())
                .defaultHeader("Authorization", "Bearer " + props.accessToken())
                .build();
    }

    @Bean(name = "apiClient")
    public WebClient apiClient(ApiProperties props) {
        return WebClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("X-Internal-Api-Key", props.internalKey())
                .build();
    }
}
