package pet.liro.chimi.domain.faq;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pet.liro.chimi.domain.common.TenantAwareEntity;

@Getter
@Setter
@Entity
@Table(name = "faqs", indexes = {
        @Index(name = "ix_faqs_tenant_category", columnList = "tenant_id, category")
})
public class Faq extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private FaqCategory category;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, length = 4000)
    private String answer;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
