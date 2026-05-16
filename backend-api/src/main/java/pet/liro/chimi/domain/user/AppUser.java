package pet.liro.chimi.domain.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

@Getter
@Setter
@Entity
@Table(name = "app_users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_tenant_email", columnNames = {"tenant_id", "email"})
})
public class AppUser extends TenantAwareEntity {

    @Column(name = "email", nullable = false, length = 200)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 200)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private UserRole role = UserRole.STAFF;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
