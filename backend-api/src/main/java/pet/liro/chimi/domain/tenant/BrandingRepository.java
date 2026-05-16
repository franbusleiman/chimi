package pet.liro.chimi.domain.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BrandingRepository extends JpaRepository<Branding, Long> {
    Optional<Branding> findByTenantId(Long tenantId);
}
