package pet.liro.chimi.domain.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByTenantIdAndPhone(Long tenantId, String phone);
    List<Conversation> findByTenantIdAndHumanHandoffTrue(Long tenantId);
}
