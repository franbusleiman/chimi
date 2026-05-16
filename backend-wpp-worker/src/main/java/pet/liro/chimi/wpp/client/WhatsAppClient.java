package pet.liro.chimi.wpp.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pet.liro.chimi.wpp.config.WppProperties;

import java.util.List;
import java.util.Map;

/**
 * Cliente para la WhatsApp Cloud API (Meta Graph).
 * Doc: https://developers.facebook.com/docs/whatsapp/cloud-api
 */
@Component
public class WhatsAppClient {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppClient.class);

    private final WebClient client;
    private final WppProperties props;

    public WhatsAppClient(@Qualifier("graphClient") WebClient client, WppProperties props) {
        this.client = client;
        this.props = props;
    }

    public void sendText(String phoneNumberId, String to, String body) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "text",
                "text", Map.of("preview_url", false, "body", body)
        );
        post(phoneNumberId, payload);
    }

    public void sendInteractiveButtons(String phoneNumberId, String to, String body, List<Map<String, String>> buttons) {
        List<Map<String, Object>> wrapped = buttons.stream()
                .map(b -> (Map<String, Object>) Map.of(
                        "type", "reply",
                        "reply", Map.of("id", b.get("id"), "title", b.get("title"))
                ))
                .toList();
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "interactive",
                "interactive", Map.of(
                        "type", "button",
                        "body", Map.of("text", body),
                        "action", Map.of("buttons", wrapped)
                )
        );
        post(phoneNumberId, payload);
    }

    public void sendInteractiveList(String phoneNumberId, String to, String headerText, String body,
                                    String buttonLabel, List<Map<String, String>> rows) {
        List<Map<String, Object>> wrappedRows = rows.stream()
                .map(r -> {
                    Map<String, Object> m = new java.util.LinkedHashMap<>();
                    m.put("id", r.get("id"));
                    m.put("title", r.get("title"));
                    if (r.containsKey("description")) m.put("description", r.get("description"));
                    return m;
                })
                .toList();
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", to,
                "type", "interactive",
                "interactive", Map.of(
                        "type", "list",
                        "header", Map.of("type", "text", "text", headerText),
                        "body", Map.of("text", body),
                        "action", Map.of(
                                "button", buttonLabel,
                                "sections", List.of(Map.of("title", headerText, "rows", wrappedRows))
                        )
                )
        );
        post(phoneNumberId, payload);
    }

    public void markRead(String phoneNumberId, String messageId) {
        post(phoneNumberId, Map.of(
                "messaging_product", "whatsapp",
                "status", "read",
                "message_id", messageId
        ));
    }

    private void post(String phoneNumberId, Map<String, Object> payload) {
        try {
            client.post()
                    .uri("/{version}/{phoneId}/messages", props.apiVersion(), phoneNumberId)
                    .bodyValue(payload)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception ex) {
            log.error("Error enviando a WhatsApp ({}): {}", phoneNumberId, ex.getMessage());
        }
    }
}
