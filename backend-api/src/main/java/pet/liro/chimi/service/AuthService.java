package pet.liro.chimi.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pet.liro.chimi.api.auth.dto.LoginRequest;
import pet.liro.chimi.api.auth.dto.LoginResponse;
import pet.liro.chimi.config.ChimiProperties;
import pet.liro.chimi.domain.tenant.Tenant;
import pet.liro.chimi.domain.tenant.TenantRepository;
import pet.liro.chimi.domain.user.AppUser;
import pet.liro.chimi.domain.user.AppUserRepository;
import pet.liro.chimi.security.JwtService;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service
public class AuthService {

    private final TenantRepository tenants;
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final ChimiProperties props;

    public AuthService(TenantRepository tenants,
                       AppUserRepository users,
                       PasswordEncoder encoder,
                       JwtService jwt,
                       ChimiProperties props) {
        this.tenants = tenants;
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.props = props;
    }

    public LoginResponse login(LoginRequest req) {
        Tenant tenant = tenants.findBySlugAndActiveTrue(req.tenantSlug())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas"));

        AppUser user = users.findByTenantIdAndEmailIgnoreCase(tenant.getId(), req.email())
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas"));

        if (!user.getActive()) {
            throw new ResponseStatusException(UNAUTHORIZED, "Usuario inactivo");
        }
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwt.generate(user);
        return new LoginResponse(
                token,
                props.jwt().expirationMinutes(),
                new LoginResponse.UserSummary(
                        user.getId(),
                        user.getTenantId(),
                        user.getEmail(),
                        user.getFullName(),
                        user.getRole().name()));
    }
}
