package pet.liro.chimi.domain.appointment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;
import pet.liro.chimi.domain.pet.Pet;
import pet.liro.chimi.domain.tutor.Tutor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "appointments", indexes = {
        @Index(name = "ix_appts_tenant_start", columnList = "tenant_id, start_at"),
        @Index(name = "ix_appts_tenant_status", columnList = "tenant_id, status")
})
public class Appointment extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "appointment_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_appts_type"))
    private AppointmentType appointmentType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_appts_pet"))
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tutor_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_appts_tutor"))
    private Tutor tutor;

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private AppointmentSource source = AppointmentSource.DASHBOARD;

    @Column(name = "notes", length = 2000)
    private String notes;

    @Column(name = "prepaid", nullable = false)
    private Boolean prepaid = false;

    @Column(name = "prepaid_amount")
    private BigDecimal prepaidAmount;

    @Column(name = "reminded_at")
    private Instant remindedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;
}
