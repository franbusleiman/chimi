package pet.liro.chimi.service;

import org.springframework.stereotype.Service;
import pet.liro.chimi.domain.appointment.Appointment;
import pet.liro.chimi.domain.appointment.AppointmentRepository;
import pet.liro.chimi.domain.appointment.AppointmentStatus;
import pet.liro.chimi.domain.tenant.Tenant;
import pet.liro.chimi.domain.tenant.TenantRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final AppointmentRepository appointments;
    private final TenantRepository tenants;

    public ReportService(AppointmentRepository appointments, TenantRepository tenants) {
        this.appointments = appointments;
        this.tenants = tenants;
    }

    public Map<String, Object> summary(Long tenantId, LocalDate from, LocalDate to) {
        Tenant t = tenants.findById(tenantId).orElseThrow();
        ZoneId zone = ZoneId.of(t.getTimezone());
        Instant start = from.atStartOfDay(zone).toInstant();
        Instant end = to.plusDays(1).atStartOfDay(zone).toInstant();

        List<Appointment> all = appointments.findByTenantIdAndStartAtBetweenOrderByStartAtAsc(tenantId, start, end);

        Map<AppointmentStatus, Long> byStatus = all.stream()
                .collect(Collectors.groupingBy(Appointment::getStatus, Collectors.counting()));
        Map<String, Long> byType = all.stream()
                .collect(Collectors.groupingBy(a -> a.getAppointmentType().getName(), Collectors.counting()));
        Map<String, Long> bySource = all.stream()
                .collect(Collectors.groupingBy(a -> a.getSource().name(), Collectors.counting()));

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("from", from);
        out.put("to", to);
        out.put("total", all.size());
        out.put("byStatus", byStatus);
        out.put("byType", byType);
        out.put("bySource", bySource);
        out.put("prepaidCount", all.stream().filter(a -> Boolean.TRUE.equals(a.getPrepaid())).count());
        return out;
    }
}
