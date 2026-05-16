package pet.liro.chimi.domain.tenant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

@Getter
@Setter
@Entity
@Table(name = "branding", uniqueConstraints = {
        @UniqueConstraint(name = "uk_branding_tenant", columnNames = "tenant_id")
})
public class Branding extends TenantAwareEntity {

    @Column(name = "display_name", nullable = false, length = 200)
    private String displayName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "primary_color", length = 9)
    private String primaryColor = "#2C7A7B";

    @Column(name = "secondary_color", length = 9)
    private String secondaryColor = "#F6AD55";

    @Column(name = "greeting", length = 1000)
    private String greeting;

    @Column(name = "tone", length = 20)
    private String tone = "informal";
}
