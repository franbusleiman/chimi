package pet.liro.chimi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.liro.chimi.api.common.NotFoundException;
import pet.liro.chimi.domain.faq.Faq;
import pet.liro.chimi.domain.faq.FaqCategory;
import pet.liro.chimi.domain.faq.FaqRepository;

import java.util.List;

@Service
public class FaqService {

    private final FaqRepository repo;

    public FaqService(FaqRepository repo) {
        this.repo = repo;
    }

    public List<Faq> listAll(Long tenantId) {
        return repo.findByTenantIdOrderByCategoryAscDisplayOrderAsc(tenantId);
    }

    public List<Faq> listActive(Long tenantId) {
        return repo.findByTenantIdAndActiveTrueOrderByCategoryAscDisplayOrderAsc(tenantId);
    }

    public List<Faq> listByCategory(Long tenantId, FaqCategory category) {
        return repo.findByTenantIdAndCategoryAndActiveTrueOrderByDisplayOrderAsc(tenantId, category);
    }

    @Transactional
    public Faq create(Long tenantId, Faq input) {
        input.setTenantId(tenantId);
        return repo.save(input);
    }

    @Transactional
    public Faq update(Long tenantId, Long id, Faq input) {
        Faq existing = repo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("FAQ no encontrada"));
        existing.setCategory(input.getCategory());
        existing.setQuestion(input.getQuestion());
        existing.setAnswer(input.getAnswer());
        existing.setDisplayOrder(input.getDisplayOrder());
        existing.setActive(input.getActive());
        return repo.save(existing);
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        Faq existing = repo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("FAQ no encontrada"));
        repo.delete(existing);
    }
}
