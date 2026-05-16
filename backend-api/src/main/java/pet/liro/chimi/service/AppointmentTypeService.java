package pet.liro.chimi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.liro.chimi.api.common.NotFoundException;
import pet.liro.chimi.domain.appointment.AppointmentType;
import pet.liro.chimi.domain.appointment.AppointmentTypeRepository;

import java.util.List;

@Service
public class AppointmentTypeService {

    private final AppointmentTypeRepository repo;

    public AppointmentTypeService(AppointmentTypeRepository repo) {
        this.repo = repo;
    }

    public List<AppointmentType> listAll(Long tenantId) {
        return repo.findByTenantIdOrderByDisplayOrderAsc(tenantId);
    }

    public List<AppointmentType> listActive(Long tenantId) {
        return repo.findByTenantIdAndActiveTrueOrderByDisplayOrderAsc(tenantId);
    }

    public AppointmentType get(Long tenantId, Long id) {
        return repo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Tipo de turno no encontrado"));
    }

    @Transactional
    public AppointmentType create(Long tenantId, AppointmentType input) {
        input.setTenantId(tenantId);
        return repo.save(input);
    }

    @Transactional
    public AppointmentType update(Long tenantId, Long id, AppointmentType input) {
        AppointmentType existing = get(tenantId, id);
        existing.setCode(input.getCode());
        existing.setName(input.getName());
        existing.setDurationMinutes(input.getDurationMinutes());
        existing.setDescription(input.getDescription());
        existing.setActive(input.getActive());
        existing.setDisplayOrder(input.getDisplayOrder());
        existing.setDefaultPrice(input.getDefaultPrice());
        return repo.save(existing);
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        AppointmentType existing = get(tenantId, id);
        existing.setActive(false);
        repo.save(existing);
    }
}
