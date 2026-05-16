package pet.liro.chimi.api.appointment;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.api.appointment.dto.AppointmentCreateRequest;
import pet.liro.chimi.api.appointment.dto.AppointmentResponse;
import pet.liro.chimi.domain.appointment.AppointmentSource;
import pet.liro.chimi.security.CurrentUser;
import pet.liro.chimi.service.AppointmentService;
import pet.liro.chimi.service.AvailabilityService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointments;
    private final AvailabilityService availability;

    public AppointmentController(AppointmentService appointments, AvailabilityService availability) {
        this.appointments = appointments;
        this.availability = availability;
    }

    @GetMapping
    public List<AppointmentResponse> list(
            @RequestParam(value = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "from", required = false) Instant from,
            @RequestParam(value = "to", required = false) Instant to) {
        Long tid = CurrentUser.require().tenantId();
        List<?> raw;
        if (date != null) {
            raw = appointments.listByDate(tid, date);
        } else if (from != null && to != null) {
            raw = appointments.listBetween(tid, from, to);
        } else {
            raw = appointments.listByDate(tid, LocalDate.now());
        }
        return raw.stream()
                .map(a -> AppointmentResponse.from((pet.liro.chimi.domain.appointment.Appointment) a))
                .toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponse get(@PathVariable Long id) {
        return AppointmentResponse.from(appointments.get(CurrentUser.require().tenantId(), id));
    }

    @PostMapping
    public AppointmentResponse create(@Valid @RequestBody AppointmentCreateRequest body) {
        return AppointmentResponse.from(
                appointments.create(CurrentUser.require().tenantId(), body, AppointmentSource.DASHBOARD));
    }

    @PostMapping("/{id}/reschedule")
    public AppointmentResponse reschedule(@PathVariable Long id, @RequestBody Map<String, Instant> body) {
        Instant newStart = body.get("startAt");
        return AppointmentResponse.from(
                appointments.reschedule(CurrentUser.require().tenantId(), id, newStart));
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.get("reason") : null;
        return AppointmentResponse.from(
                appointments.cancel(CurrentUser.require().tenantId(), id, reason));
    }

    @PostMapping("/{id}/attended")
    public AppointmentResponse attended(@PathVariable Long id) {
        return AppointmentResponse.from(
                appointments.markAttended(CurrentUser.require().tenantId(), id));
    }

    @PostMapping("/{id}/no-show")
    public AppointmentResponse noShow(@PathVariable Long id) {
        return AppointmentResponse.from(
                appointments.markNoShow(CurrentUser.require().tenantId(), id));
    }

    @GetMapping("/available-slots")
    public List<Instant> availableSlots(
            @RequestParam("appointmentTypeId") Long appointmentTypeId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availability.availableSlots(CurrentUser.require().tenantId(), appointmentTypeId, date);
    }
}
