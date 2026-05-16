package pet.liro.chimi.domain.appointment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentTypeRepository extends JpaRepository<AppointmentType, Long> {
    List<AppointmentType> findByTenantIdOrderByDisplayOrderAsc(Long tenantId);
    List<AppointmentType> findByTenantIdAndActiveTrueOrderByDisplayOrderAsc(Long tenantId);
    Optional<AppointmentType> findByTenantIdAndCode(Long tenantId, String code);
    Optional<AppointmentType> findByIdAndTenantId(Long id, Long tenantId);
}
