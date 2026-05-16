package pet.liro.chimi.api.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String tenantSlug,
        @NotBlank String email,
        @NotBlank String password
) {}
