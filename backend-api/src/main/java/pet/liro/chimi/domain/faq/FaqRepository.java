package pet.liro.chimi.domain.faq;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FaqRepository extends JpaRepository<Faq, Long> {
    Optional<Faq> findByIdAndTenantId(Long id, Long tenantId);
    List<Faq> findByTenantIdAndActiveTrueOrderByCategoryAscDisplayOrderAsc(Long tenantId);
    List<Faq> findByTenantIdAndCategoryAndActiveTrueOrderByDisplayOrderAsc(Long tenantId, FaqCategory category);
    List<Faq> findByTenantIdOrderByCategoryAscDisplayOrderAsc(Long tenantId);
}
