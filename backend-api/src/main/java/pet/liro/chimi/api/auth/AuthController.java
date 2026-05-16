package pet.liro.chimi.api.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.api.auth.dto.LoginRequest;
import pet.liro.chimi.api.auth.dto.LoginResponse;
import pet.liro.chimi.security.AuthPrincipal;
import pet.liro.chimi.security.CurrentUser;
import pet.liro.chimi.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public AuthPrincipal me() {
        return CurrentUser.require();
    }
}
