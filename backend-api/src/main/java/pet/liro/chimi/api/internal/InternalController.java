package pet.liro.chimi.api.internal;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.api.appointment.dto.AppointmentCreateRequest;
import pet.liro.chimi.api.appointment.dto.AppointmentResponse;
import pet.liro.chimi.api.common.NotFoundException;
import pet.liro.chimi.domain.appointment.AppointmentSource;
import pet.liro.chimi.domain.appointment.AppointmentType;
import pet.liro.chimi.domain.faq.Faq;
import pet.liro.chimi.domain.faq.FaqCategory;
import pet.liro.chimi.domain.tenant.Tenant;
import pet.liro.chimi.domain.tenant.TenantRepository;
import pet.liro.chimi.service.AppointmentService;
import pet.liro.chimi.service.AppointmentTypeService;
import pet.liro.chimi.service.AvailabilityService;
import pet.liro.chimi.service.FaqService;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints internos consumidos por wpp-worker.
 * Protegidos por {@link InternalApiKeyFilter} (header X-Internal-Api-Key).
 * El tenant se identifica por slug, por phone_number_id o por id explícito en la URL.
 */
@RestController
@RequestMapping("/api/internal")
public class InternalController {

    private final TenantRepository tenants;
    private final AppointmentTypeService types;
    private final AvailabilityService availability;
    private final AppointmentService appointments;
    private final FaqService faqs;

    public InternalController(TenantRepository tenants,
                              AppointmentTypeService types,
                              AvailabilityService availability,
                              AppointmentService appointments,
                              FaqService faqs) {
        this.tenants = tenants;
        this.types = types;
        this.availability = availability;
        this.appointments = appointments;
        this.faqs = faqs;
    }

    @GetMapping("/tenants/by-phone-number-id/{phoneNumberId}")
    public Tenant tenantByPhoneNumberId(@PathVariable String phoneNumberId) {
        return tenants.findByWppPhoneNumberId(phoneNumberId)
                .orElseThrow(() -> new NotFoundException("Tenant para phone_number_id=" + phoneNumberId));
    }

    @GetMapping("/tenants/by-slug/{slug}")
    public Tenant tenantBySlug(@PathVariable String slug) {
        return tenants.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Tenant slug=" + slug));
    }

    @GetMapping("/tenants/{tenantId}/appointment-types")
    public List<AppointmentType> apptTypes(@PathVariable Long tenantId) {
        return types.listActive(tenantId);
    }

    @GetMapping("/tenants/{tenantId}/available-slots")
    public List<Instant> availableSlots(@PathVariable Long tenantId,
                                        @RequestParam("appointmentTypeId") Long appointmentTypeId,
                                        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availability.availableSlots(tenantId, appointmentTypeId, date);
    }

    @PostMapping("/tenants/{tenantId}/appointments")
    public AppointmentResponse createAppointment(@PathVariable Long tenantId,
                                                 @RequestBody AppointmentCreateRequest req) {
        return AppointmentResponse.from(
                appointments.create(tenantId, req, AppointmentSource.WHATSAPP));
    }

    @PostMapping("/tenants/{tenantId}/appointments/{id}/cancel")
    public AppointmentResponse cancelAppointment(@PathVariable Long tenantId,
                                                 @PathVariable Long id,
                                                 @RequestParam(value = "reason", required = false) String reason) {
        return AppointmentResponse.from(appointments.cancel(tenantId, id, reason));
    }

    @GetMapping("/tenants/{tenantId}/faqs")
    public List<Faq> faqs(@PathVariable Long tenantId,
                          @RequestParam(value = "category", required = false) FaqCategory category) {
        return category != null ? faqs.listByCategory(tenantId, category) : faqs.listActive(tenantId);
    }
}
