package pet.liro.chimi.wpp.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByTenantIdAndPhone(Long tenantId, String phone);
}
