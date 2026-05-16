package pet.liro.chimi.domain.conversation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conv_tenant_phone", columnNames = {"tenant_id", "phone"})
})
public class Conversation extends TenantAwareEntity {

    @Column(name = "phone", nullable = false, length = 30)
    private String phone;

    @Column(name = "state", nullable = false, length = 50)
    private String state = "IDLE";

    @Lob
    @Column(name = "context_json")
    private String contextJson;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @Column(name = "human_handoff", nullable = false)
    private Boolean humanHandoff = false;

    @Column(name = "handoff_at")
    private Instant handoffAt;
}
