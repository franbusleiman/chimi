package pet.liro.chimi.wpp.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class ChimiApiClient {

    private final WebClient client;

    public ChimiApiClient(@Qualifier("apiClient") WebClient client) {
        this.client = client;
    }

    public Map<String, Object> tenantByPhoneNumberId(String phoneNumberId) {
        return client.get()
                .uri("/api/internal/tenants/by-phone-number-id/{id}", phoneNumberId)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> appointmentTypes(Long tenantId) {
        return client.get()
                .uri("/api/internal/tenants/{id}/appointment-types", tenantId)
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public List<Instant> availableSlots(Long tenantId, Long appointmentTypeId, LocalDate date) {
        return client.get()
                .uri(uri -> uri.path("/api/internal/tenants/{id}/available-slots")
                        .queryParam("appointmentTypeId", appointmentTypeId)
                        .queryParam("date", date)
                        .build(tenantId))
                .retrieve()
                .bodyToMono(List.class)
                .map(list -> (List<Instant>) list.stream()
                        .map(s -> Instant.parse(s.toString()))
                        .toList())
                .block();
    }

    public Map<String, Object> createAppointment(Long tenantId, Map<String, Object> body) {
        return client.post()
                .uri("/api/internal/tenants/{id}/appointments", tenantId)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> faqs(Long tenantId, String category) {
        return client.get()
                .uri(uri -> uri.path("/api/internal/tenants/{id}/faqs")
                        .queryParamIfPresent("category", java.util.Optional.ofNullable(category))
                        .build(tenantId))
                .retrieve()
                .bodyToMono(List.class)
                .block();
    }
}
