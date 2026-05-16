package pet.liro.chimi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chimi")
public record ChimiProperties(
        Jwt jwt,
        Internal internal
) {
    public record Jwt(String secret, long expirationMinutes) {}
    public record Internal(String apiKey) {}
}
