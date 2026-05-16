package pet.liro.chimi.api.auth.dto;

public record LoginResponse(
        String token,
        long expiresInMinutes,
        UserSummary user
) {
    public record UserSummary(Long id, Long tenantId, String email, String fullName, String role) {}
}
