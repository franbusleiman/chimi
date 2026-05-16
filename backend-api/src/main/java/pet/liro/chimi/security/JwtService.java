package pet.liro.chimi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import pet.liro.chimi.config.ChimiProperties;
import pet.liro.chimi.domain.user.AppUser;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMinutes;

    public JwtService(ChimiProperties props) {
        byte[] secret = props.jwt().secret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException(
                    "chimi.jwt.secret debe tener al menos 32 bytes — configurá JWT_SECRET con un valor largo (ej. openssl rand -base64 48)");
        }
        this.key = Keys.hmacShaKeyFor(secret);
        this.expirationMinutes = props.jwt().expirationMinutes();
    }

    public String generate(AppUser user) {
        Instant now = Instant.now();
        Instant exp = now.plus(expirationMinutes, ChronoUnit.MINUTES);
        return Jwts.builder()
                .subject(user.getId().toString())
                .claims(Map.of(
                        "tid", user.getTenantId(),
                        "email", user.getEmail(),
                        "name", user.getFullName(),
                        "role", user.getRole().name()
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
