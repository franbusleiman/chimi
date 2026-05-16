package pet.liro.chimi.domain.conversation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WppMessageRepository extends JpaRepository<WppMessage, Long> {
    List<WppMessage> findByConversationIdOrderBySentAtAsc(Long conversationId);
}
