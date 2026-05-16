package pet.liro.chimi.wpp.bot;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WebhookParser {

    /**
     * Aplana el payload del webhook de Cloud API y devuelve los mensajes entrantes encontrados.
     * Cloud API puede agrupar varios mensajes en un solo POST.
     */
    public List<IncomingMessage> parse(JsonNode root) {
        List<IncomingMessage> out = new ArrayList<>();
        JsonNode entries = root.path("entry");
        if (!entries.isArray()) return out;

        for (JsonNode entry : entries) {
            for (JsonNode change : entry.path("changes")) {
                JsonNode value = change.path("value");
                String phoneNumberId = value.path("metadata").path("phone_number_id").asText(null);
                for (JsonNode msg : value.path("messages")) {
                    String from = msg.path("from").asText(null);
                    String id = msg.path("id").asText(null);
                    String type = msg.path("type").asText(null);
                    String text = null, buttonId = null, listId = null;
                    if ("text".equals(type)) {
                        text = msg.path("text").path("body").asText(null);
                    } else if ("interactive".equals(type)) {
                        JsonNode interactive = msg.path("interactive");
                        String itype = interactive.path("type").asText(null);
                        if ("button_reply".equals(itype)) {
                            buttonId = interactive.path("button_reply").path("id").asText(null);
                            text = interactive.path("button_reply").path("title").asText(null);
                        } else if ("list_reply".equals(itype)) {
                            listId = interactive.path("list_reply").path("id").asText(null);
                            text = interactive.path("list_reply").path("title").asText(null);
                        }
                    } else if ("button".equals(type)) {
                        buttonId = msg.path("button").path("payload").asText(null);
                        text = msg.path("button").path("text").asText(null);
                    }
                    out.add(new IncomingMessage(phoneNumberId, from, id, type, text, buttonId, listId));
                }
            }
        }
        return out;
    }
}
