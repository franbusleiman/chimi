package pet.liro.chimi.domain.tutor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TutorRepository extends JpaRepository<Tutor, Long> {
    Optional<Tutor> findByTenantIdAndPhone(Long tenantId, String phone);
    List<Tutor> findByTenantId(Long tenantId);
}
