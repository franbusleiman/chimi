package pet.liro.chimi.security;

import pet.liro.chimi.domain.user.UserRole;

public record AuthPrincipal(Long userId, Long tenantId, String email, String fullName, UserRole role) {
}
