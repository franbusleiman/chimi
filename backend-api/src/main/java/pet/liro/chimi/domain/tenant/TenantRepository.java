package pet.liro.chimi.domain.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findBySlug(String slug);
    Optional<Tenant> findBySlugAndActiveTrue(String slug);
    Optional<Tenant> findByWppPhoneNumberId(String wppPhoneNumberId);
}
