package pet.liro.chimi.api.appointment;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.domain.appointment.AppointmentType;
import pet.liro.chimi.security.CurrentUser;
import pet.liro.chimi.service.AppointmentTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/appointment-types")
public class AppointmentTypeController {

    private final AppointmentTypeService service;

    public AppointmentTypeController(AppointmentTypeService service) {
        this.service = service;
    }

    @GetMapping
    public List<AppointmentType> list(@RequestParam(value = "active", required = false) Boolean active) {
        Long tid = CurrentUser.require().tenantId();
        return Boolean.TRUE.equals(active) ? service.listActive(tid) : service.listAll(tid);
    }

    @GetMapping("/{id}")
    public AppointmentType get(@PathVariable Long id) {
        return service.get(CurrentUser.require().tenantId(), id);
    }

    @PostMapping
    public AppointmentType create(@Valid @RequestBody AppointmentType body) {
        return service.create(CurrentUser.require().tenantId(), body);
    }

    @PutMapping("/{id}")
    public AppointmentType update(@PathVariable Long id, @Valid @RequestBody AppointmentType body) {
        return service.update(CurrentUser.require().tenantId(), id, body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(CurrentUser.require().tenantId(), id);
    }
}
