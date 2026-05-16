package pet.liro.chimi.domain.tutor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

@Getter
@Setter
@Entity
@Table(name = "tutors", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tutors_tenant_phone", columnNames = {"tenant_id", "phone"})
})
public class Tutor extends TenantAwareEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "email", length = 200)
    private String email;

    @Column(name = "notes", length = 1000)
    private String notes;
}
