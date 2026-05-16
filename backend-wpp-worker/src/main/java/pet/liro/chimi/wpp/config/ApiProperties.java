package pet.liro.chimi.wpp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "chimi.api")
public record ApiProperties(
        String baseUrl,
        String internalKey
) {}
