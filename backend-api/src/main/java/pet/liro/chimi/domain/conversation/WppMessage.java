package pet.liro.chimi.domain.conversation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "wpp_messages", indexes = {
        @Index(name = "ix_msgs_conv_ts", columnList = "conversation_id, sent_at")
})
public class WppMessage extends TenantAwareEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_msgs_conv"))
    private Conversation conversation;

    @Column(name = "direction", nullable = false, length = 10)
    private String direction;

    @Column(name = "wpp_message_id", length = 100)
    private String wppMessageId;

    @Column(name = "type", length = 30)
    private String type;

    @Lob
    @Column(name = "body")
    private String body;

    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;
}
