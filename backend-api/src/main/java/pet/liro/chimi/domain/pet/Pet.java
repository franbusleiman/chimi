package pet.liro.chimi.domain.pet;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;
import pet.liro.chimi.domain.tutor.Tutor;

@Getter
@Setter
@Entity
@Table(name = "pets")
public class Pet extends TenantAwareEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "species", length = 50)
    private String species;

    @Column(name = "breed", length = 100)
    private String breed;

    @Column(name = "notes", length = 1000)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pets_tutor"))
    private Tutor tutor;
}
