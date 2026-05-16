package pet.liro.chimi.domain.block;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "schedule_blocks", indexes = {
        @Index(name = "ix_blocks_tenant_start", columnList = "tenant_id, start_at")
})
public class Block extends TenantAwareEntity {

    @Column(name = "start_at", nullable = false)
    private Instant startAt;

    @Column(name = "end_at", nullable = false)
    private Instant endAt;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}
