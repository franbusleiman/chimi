package pet.liro.chimi.domain.appointment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

@Getter
@Setter
@Entity
@Table(name = "appointment_types", uniqueConstraints = {
        @UniqueConstraint(name = "uk_apt_types_tenant_code", columnNames = {"tenant_id", "code"})
})
public class AppointmentType extends TenantAwareEntity {

    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "default_price")
    private java.math.BigDecimal defaultPrice;
}
