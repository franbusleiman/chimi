package pet.liro.chimi.domain.tenant;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.BaseEntity;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "tenants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_tenants_slug", columnNames = "slug")
})
public class Tenant extends BaseEntity {

    @Column(name = "slug", nullable = false, length = 60)
    private String slug;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "timezone", nullable = false, length = 60)
    private String timezone = "America/Argentina/Buenos_Aires";

    @Column(name = "wpp_phone_number_id", length = 60)
    private String wppPhoneNumberId;

    @Column(name = "wpp_business_account_id", length = 60)
    private String wppBusinessAccountId;

    @Column(name = "parallel_slots", nullable = false)
    private Integer parallelSlots = 1;

    @Column(name = "min_lead_minutes", nullable = false)
    private Integer minLeadMinutes = 60;

    @Column(name = "max_lead_days", nullable = false)
    private Integer maxLeadDays = 60;

    @Column(name = "slot_granularity_minutes", nullable = false)
    private Integer slotGranularityMinutes = 15;

    @Column(name = "allow_cancel_by_wpp", nullable = false)
    private Boolean allowCancelByWpp = true;

    @Column(name = "allow_reschedule_by_wpp", nullable = false)
    private Boolean allowRescheduleByWpp = true;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
