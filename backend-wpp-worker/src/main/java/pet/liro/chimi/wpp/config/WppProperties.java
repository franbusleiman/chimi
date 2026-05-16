package pet.liro.chimi.wpp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chimi.wpp")
public record WppProperties(
        String verifyToken,
        String accessToken,
        String phoneNumberId,
        String businessAccountId,
        String apiVersion,
        String graphBaseUrl
) {}
