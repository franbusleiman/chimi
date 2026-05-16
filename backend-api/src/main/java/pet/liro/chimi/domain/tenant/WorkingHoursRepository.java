package pet.liro.chimi.domain.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkingHoursRepository extends JpaRepository<WorkingHours, Long> {
    List<WorkingHours> findByTenantId(Long tenantId);
}
