package pet.liro.chimi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.liro.chimi.api.common.NotFoundException;
import pet.liro.chimi.domain.tenant.Branding;
import pet.liro.chimi.domain.tenant.BrandingRepository;

@Service
public class BrandingService {

    private final BrandingRepository repo;

    public BrandingService(BrandingRepository repo) {
        this.repo = repo;
    }

    public Branding get(Long tenantId) {
        return repo.findByTenantId(tenantId)
                .orElseThrow(() -> new NotFoundException("Branding no configurado para este tenant"));
    }

    @Transactional
    public Branding upsert(Long tenantId, Branding input) {
        Branding existing = repo.findByTenantId(tenantId).orElseGet(() -> {
            Branding b = new Branding();
            b.setTenantId(tenantId);
            return b;
        });
        existing.setDisplayName(input.getDisplayName());
        existing.setLogoUrl(input.getLogoUrl());
        existing.setPrimaryColor(input.getPrimaryColor());
        existing.setSecondaryColor(input.getSecondaryColor());
        existing.setGreeting(input.getGreeting());
        existing.setTone(input.getTone());
        return repo.save(existing);
    }
}
