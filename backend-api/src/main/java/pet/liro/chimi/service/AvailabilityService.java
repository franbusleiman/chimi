package pet.liro.chimi.service;

import org.springframework.stereotype.Service;
import pet.liro.chimi.api.common.NotFoundException;
import pet.liro.chimi.domain.appointment.AppointmentType;
import pet.liro.chimi.domain.appointment.AppointmentTypeRepository;
import pet.liro.chimi.domain.block.BlockRepository;
import pet.liro.chimi.domain.appointment.AppointmentRepository;
import pet.liro.chimi.domain.tenant.Tenant;
import pet.liro.chimi.domain.tenant.TenantRepository;
import pet.liro.chimi.domain.tenant.WorkingHours;
import pet.liro.chimi.domain.tenant.WorkingHoursRepository;

import java.time.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class AvailabilityService {

    private final TenantRepository tenants;
    private final WorkingHoursRepository workingHours;
    private final AppointmentTypeRepository types;
    private final AppointmentRepository appointments;
    private final BlockRepository blocks;

    public AvailabilityService(TenantRepository tenants,
                               WorkingHoursRepository workingHours,
                               AppointmentTypeRepository types,
                               AppointmentRepository appointments,
                               BlockRepository blocks) {
        this.tenants = tenants;
        this.workingHours = workingHours;
        this.types = types;
        this.appointments = appointments;
        this.blocks = blocks;
    }

    public List<Instant> availableSlots(Long tenantId, Long appointmentTypeId, LocalDate date) {
        Tenant tenant = tenants.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant no encontrado"));
        AppointmentType type = types.findByIdAndTenantId(appointmentTypeId, tenantId)
                .orElseThrow(() -> new NotFoundException("Tipo de turno no encontrado"));

        ZoneId zone = ZoneId.of(tenant.getTimezone());
        DayOfWeek dow = date.getDayOfWeek();

        List<WorkingHours> ranges = workingHours.findByTenantId(tenantId).stream()
                .filter(w -> w.getDayOfWeek() == dow)
                .toList();
        if (ranges.isEmpty()) return List.of();

        int duration = type.getDurationMinutes();
        int granularity = tenant.getSlotGranularityMinutes();
        int parallel = tenant.getParallelSlots();

        Instant now = Instant.now();
        Instant minStart = now.plus(Duration.ofMinutes(tenant.getMinLeadMinutes()));
        Instant maxStart = LocalDate.now(zone).plusDays(tenant.getMaxLeadDays())
                .atStartOfDay(zone).toInstant();

        List<Instant> slots = new ArrayList<>();
        for (WorkingHours range : ranges) {
            ZonedDateTime open = ZonedDateTime.of(date, range.getStartTime(), zone);
            ZonedDateTime close = ZonedDateTime.of(date, range.getEndTime(), zone);
            ZonedDateTime cursor = open;
            while (!cursor.plusMinutes(duration).isAfter(close)) {
                Instant start = cursor.toInstant();
                Instant end = cursor.plusMinutes(duration).toInstant();

                boolean inRange = !start.isBefore(minStart) && !start.isAfter(maxStart);
                boolean blocked = !blocks.findOverlapping(tenantId, start, end).isEmpty();
                long busy = appointments.countOverlapping(tenantId, start, end);

                if (inRange && !blocked && busy < parallel) {
                    slots.add(start);
                }
                cursor = cursor.plusMinutes(granularity);
            }
        }
        return slots;
    }
}
