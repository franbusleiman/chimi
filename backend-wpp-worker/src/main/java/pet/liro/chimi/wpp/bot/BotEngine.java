package pet.liro.chimi.wpp.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.liro.chimi.wpp.client.ChimiApiClient;
import pet.liro.chimi.wpp.client.WhatsAppClient;
import pet.liro.chimi.wpp.domain.Conversation;
import pet.liro.chimi.wpp.domain.ConversationRepository;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Motor de flujo conversacional sin IA. Árbol de decisión basado en estado.
 *
 * Flujo principal:
 *   IDLE -> MAIN_MENU
 *   MAIN_MENU [turno|faq|humano]
 *     turno  -> APPT_PICK_TYPE -> APPT_PICK_DATE -> APPT_PICK_SLOT
 *             -> APPT_TUTOR_FIRST_NAME -> _LAST_NAME -> APPT_PET_FIRST_NAME -> _LAST_NAME
 *             -> APPT_CONFIRM -> IDLE
 *     faq    -> FAQ_PICK_CATEGORY -> FAQ_PICK_QUESTION -> MAIN_MENU
 *     humano -> HUMAN_HANDOFF
 */
@Service
public class BotEngine {

    private static final Logger log = LoggerFactory.getLogger(BotEngine.class);
    private static final DateTimeFormatter D_DM = DateTimeFormatter.ofPattern("dd/MM");
    private static final DateTimeFormatter D_HM = DateTimeFormatter.ofPattern("HH:mm");

    private final WebhookParser parser;
    private final ConversationRepository conversations;
    private final ChimiApiClient api;
    private final WhatsAppClient wpp;
    private final ObjectMapper mapper = new ObjectMapper();

    public BotEngine(WebhookParser parser,
                     ConversationRepository conversations,
                     ChimiApiClient api,
                     WhatsAppClient wpp) {
        this.parser = parser;
        this.conversations = conversations;
        this.api = api;
        this.wpp = wpp;
    }

    @Transactional
    public void handleIncoming(JsonNode payload) {
        List<IncomingMessage> messages = parser.parse(payload);
        for (IncomingMessage msg : messages) {
            try {
                processOne(msg);
            } catch (Exception ex) {
                log.error("Error procesando mensaje de {}: {}", msg.fromPhone(), ex.getMessage(), ex);
                wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                        "Tuvimos un problema procesando tu mensaje. Probá de nuevo en un rato 🙏");
            }
        }
    }

    private void processOne(IncomingMessage msg) {
        Map<String, Object> tenant = api.tenantByPhoneNumberId(msg.phoneNumberId());
        if (tenant == null) {
            log.warn("No hay tenant para phone_number_id={}", msg.phoneNumberId());
            return;
        }
        Long tenantId = ((Number) tenant.get("id")).longValue();
        ZoneId tz = ZoneId.of((String) tenant.getOrDefault("timezone", "America/Argentina/Buenos_Aires"));

        Conversation conv = conversations.findByTenantIdAndPhone(tenantId, msg.fromPhone())
                .orElseGet(() -> {
                    Conversation c = new Conversation();
                    c.setTenantId(tenantId);
                    c.setPhone(msg.fromPhone());
                    return c;
                });
        conv.setLastMessageAt(Instant.now());

        if (Boolean.TRUE.equals(conv.getHumanHandoff())) {
            conversations.save(conv);
            return;
        }

        wpp.markRead(msg.phoneNumberId(), msg.messageId());

        BotContext ctx = loadContext(conv);
        BotState state = parseState(conv.getState());
        String input = pickInput(msg);

        if (isGlobalReset(input)) {
            state = BotState.MAIN_MENU;
        }

        state = step(msg, tenantId, tz, conv, ctx, state, input);
        conv.setState(state.name());
        conv.setContextJson(saveContext(ctx));
        conversations.save(conv);
    }

    private BotState step(IncomingMessage msg, Long tenantId, ZoneId tz,
                          Conversation conv, BotContext ctx, BotState state, String input) {
        return switch (state) {
            case IDLE -> showMainMenu(msg);
            case MAIN_MENU -> handleMainMenu(msg, tenantId, ctx, input);
            case APPT_PICK_TYPE -> handlePickType(msg, tenantId, tz, ctx, input);
            case APPT_PICK_DATE -> handlePickDate(msg, tenantId, tz, ctx, input);
            case APPT_PICK_SLOT -> handlePickSlot(msg, tz, ctx, input);
            case APPT_TUTOR_FIRST_NAME -> {
                ctx.tutorFirstName = input;
                wpp.sendText(msg.phoneNumberId(), msg.fromPhone(), "Apellido del tutor:");
                yield BotState.APPT_TUTOR_LAST_NAME;
            }
            case APPT_TUTOR_LAST_NAME -> {
                ctx.tutorLastName = input;
                wpp.sendText(msg.phoneNumberId(), msg.fromPhone(), "Nombre de la mascota:");
                yield BotState.APPT_PET_FIRST_NAME;
            }
            case APPT_PET_FIRST_NAME -> {
                ctx.petFirstName = input;
                wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                        "Apellido de la mascota (si no tiene, escribí \"-\"):");
                yield BotState.APPT_PET_LAST_NAME;
            }
            case APPT_PET_LAST_NAME -> {
                ctx.petLastName = "-".equals(input) ? null : input;
                yield confirmAppointment(msg, tz, ctx);
            }
            case APPT_CONFIRM -> handleConfirm(msg, tenantId, conv, ctx, input);
            case FAQ_PICK_CATEGORY -> handlePickFaqCategory(msg, tenantId, input);
            case FAQ_PICK_QUESTION -> handlePickFaqQuestion(msg, tenantId, input);
            case HUMAN_HANDOFF -> state;
        };
    }

    // ------- pantallas -------

    private BotState showMainMenu(IncomingMessage msg) {
        wpp.sendInteractiveButtons(msg.phoneNumberId(), msg.fromPhone(),
                "¡Hola! ¿En qué te puedo ayudar?",
                List.of(
                        Map.of("id", "menu_turno", "title", "Sacar turno"),
                        Map.of("id", "menu_faq", "title", "Consultas"),
                        Map.of("id", "menu_humano", "title", "Hablar con alguien")
                ));
        return BotState.MAIN_MENU;
    }

    private BotState handleMainMenu(IncomingMessage msg, Long tenantId, BotContext ctx, String input) {
        if (input == null) return showMainMenu(msg);
        return switch (input) {
            case "menu_turno" -> listAppointmentTypes(msg, tenantId, ctx);
            case "menu_faq" -> askFaqCategory(msg);
            case "menu_humano" -> handoff(msg);
            default -> showMainMenu(msg);
        };
    }

    private BotState listAppointmentTypes(IncomingMessage msg, Long tenantId, BotContext ctx) {
        var types = api.appointmentTypes(tenantId);
        if (types == null || types.isEmpty()) {
            wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                    "Por ahora no hay tipos de turno disponibles. Te conectamos con una persona.");
            return handoff(msg);
        }
        var rows = types.stream().limit(10).map(t -> {
            Map<String, String> row = new HashMap<>();
            row.put("id", "type_" + t.get("id"));
            row.put("title", String.valueOf(t.get("name")));
            row.put("description", t.get("durationMinutes") + " min");
            return row;
        }).toList();
        wpp.sendInteractiveList(msg.phoneNumberId(), msg.fromPhone(),
                "Tipos de turno", "Elegí el tipo de turno:", "Ver opciones", rows);
        return BotState.APPT_PICK_TYPE;
    }

    private BotState handlePickType(IncomingMessage msg, Long tenantId, ZoneId tz, BotContext ctx, String input) {
        if (input == null || !input.startsWith("type_")) {
            return listAppointmentTypes(msg, tenantId, ctx);
        }
        Long id = Long.valueOf(input.substring("type_".length()));
        var types = api.appointmentTypes(tenantId);
        var chosen = types.stream()
                .filter(t -> ((Number) t.get("id")).longValue() == id)
                .findFirst().orElse(null);
        if (chosen == null) return listAppointmentTypes(msg, tenantId, ctx);
        ctx.appointmentTypeId = id;
        ctx.appointmentTypeName = String.valueOf(chosen.get("name"));
        ctx.durationMinutes = ((Number) chosen.get("durationMinutes")).intValue();
        return askDate(msg, tz);
    }

    private BotState askDate(IncomingMessage msg, ZoneId tz) {
        LocalDate today = LocalDate.now(tz);
        List<Map<String, String>> rows = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate d = today.plusDays(i);
            Map<String, String> r = new HashMap<>();
            r.put("id", "date_" + d);
            r.put("title", d.format(D_DM) + " " + dayNameEs(d.getDayOfWeek()));
            rows.add(r);
        }
        wpp.sendInteractiveList(msg.phoneNumberId(), msg.fromPhone(),
                "Elegí día", "¿Qué día querés venir?", "Ver días", rows);
        return BotState.APPT_PICK_DATE;
    }

    private BotState handlePickDate(IncomingMessage msg, Long tenantId, ZoneId tz, BotContext ctx, String input) {
        if (input == null || !input.startsWith("date_")) return askDate(msg, tz);
        ctx.selectedDate = LocalDate.parse(input.substring("date_".length()));
        var slots = api.availableSlots(tenantId, ctx.appointmentTypeId, ctx.selectedDate);
        if (slots == null || slots.isEmpty()) {
            wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                    "No hay horarios disponibles para " + ctx.selectedDate.format(D_DM) + ". Probá con otro día.");
            return askDate(msg, tz);
        }
        var rows = slots.stream().limit(10).map(s -> {
            Map<String, String> r = new HashMap<>();
            r.put("id", "slot_" + s.toString());
            r.put("title", s.atZone(tz).format(D_HM));
            return r;
        }).toList();
        wpp.sendInteractiveList(msg.phoneNumberId(), msg.fromPhone(),
                "Horarios", "Elegí horario para el " + ctx.selectedDate.format(D_DM) + ":",
                "Ver horarios", rows);
        return BotState.APPT_PICK_SLOT;
    }

    private BotState handlePickSlot(IncomingMessage msg, ZoneId tz, BotContext ctx, String input) {
        if (input == null || !input.startsWith("slot_")) return BotState.APPT_PICK_SLOT;
        ctx.selectedSlot = Instant.parse(input.substring("slot_".length()));
        wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                "Perfecto. Para registrarte, ¿cuál es tu nombre?");
        return BotState.APPT_TUTOR_FIRST_NAME;
    }

    private BotState confirmAppointment(IncomingMessage msg, ZoneId tz, BotContext ctx) {
        String resumen = String.format("""
                Confirmá el turno:
                • Tipo: %s
                • Fecha: %s
                • Horario: %s
                • Mascota: %s%s
                • Tutor: %s %s
                """,
                ctx.appointmentTypeName,
                ctx.selectedDate.format(D_DM),
                ctx.selectedSlot.atZone(tz).format(D_HM),
                ctx.petFirstName,
                ctx.petLastName != null ? " " + ctx.petLastName : "",
                ctx.tutorFirstName, ctx.tutorLastName);
        wpp.sendInteractiveButtons(msg.phoneNumberId(), msg.fromPhone(), resumen,
                List.of(
                        Map.of("id", "confirm_yes", "title", "Confirmar"),
                        Map.of("id", "confirm_no", "title", "Cancelar")
                ));
        return BotState.APPT_CONFIRM;
    }

    private BotState handleConfirm(IncomingMessage msg, Long tenantId, Conversation conv,
                                   BotContext ctx, String input) {
        if ("confirm_no".equals(input)) {
            wpp.sendText(msg.phoneNumberId(), msg.fromPhone(), "Listo, cancelamos. Escribí *menú* para volver.");
            return BotState.IDLE;
        }
        if (!"confirm_yes".equals(input)) {
            return BotState.APPT_CONFIRM;
        }
        Map<String, Object> body = new HashMap<>();
        body.put("appointmentTypeId", ctx.appointmentTypeId);
        body.put("startAt", ctx.selectedSlot.toString());
        body.put("tutorFirstName", ctx.tutorFirstName);
        body.put("tutorLastName", ctx.tutorLastName);
        body.put("tutorPhone", conv.getPhone());
        body.put("petFirstName", ctx.petFirstName);
        body.put("petLastName", ctx.petLastName);
        body.put("source", "WHATSAPP");
        try {
            var created = api.createAppointment(tenantId, body);
            wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                    "¡Listo! Turno confirmado ✅ Te esperamos. Escribí *menú* si necesitás otra cosa.");
        } catch (Exception ex) {
            log.error("Error creando turno: {}", ex.getMessage());
            wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                    "No pudimos confirmar el turno (puede que se haya ocupado). Te conectamos con una persona.");
            return handoff(msg);
        }
        return BotState.IDLE;
    }

    private BotState askFaqCategory(IncomingMessage msg) {
        wpp.sendInteractiveButtons(msg.phoneNumberId(), msg.fromPhone(),
                "¿Sobre qué querés consultar?",
                List.of(
                        Map.of("id", "faq_CLINIC", "title", "Clínica"),
                        Map.of("id", "faq_PRODUCTS", "title", "Productos"),
                        Map.of("id", "menu_back", "title", "Volver")
                ));
        return BotState.FAQ_PICK_CATEGORY;
    }

    private BotState handlePickFaqCategory(IncomingMessage msg, Long tenantId, String input) {
        if (input == null || !input.startsWith("faq_")) return askFaqCategory(msg);
        String cat = input.substring("faq_".length());
        var list = api.faqs(tenantId, cat);
        if (list == null || list.isEmpty()) {
            wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                    "Por ahora no hay preguntas frecuentes en esa categoría.");
            return showMainMenu(msg);
        }
        var rows = list.stream().limit(10).map(f -> {
            Map<String, String> r = new HashMap<>();
            r.put("id", "faqq_" + f.get("id"));
            String q = String.valueOf(f.get("question"));
            r.put("title", q.length() > 24 ? q.substring(0, 24) : q);
            r.put("description", q.length() > 24 ? q.substring(0, Math.min(72, q.length())) : "");
            return r;
        }).toList();
        wpp.sendInteractiveList(msg.phoneNumberId(), msg.fromPhone(),
                "Preguntas", "Elegí una pregunta:", "Ver preguntas", rows);
        return BotState.FAQ_PICK_QUESTION;
    }

    private BotState handlePickFaqQuestion(IncomingMessage msg, Long tenantId, String input) {
        if (input == null || !input.startsWith("faqq_")) return BotState.FAQ_PICK_QUESTION;
        Long id = Long.valueOf(input.substring("faqq_".length()));
        var faqs = api.faqs(tenantId, null);
        var faq = faqs.stream().filter(f -> ((Number) f.get("id")).longValue() == id).findFirst().orElse(null);
        if (faq == null) {
            wpp.sendText(msg.phoneNumberId(), msg.fromPhone(), "No encontré esa pregunta, ¿probás de nuevo?");
            return askFaqCategory(msg);
        }
        wpp.sendText(msg.phoneNumberId(), msg.fromPhone(), String.valueOf(faq.get("answer")));
        return showMainMenu(msg);
    }

    private BotState handoff(IncomingMessage msg) {
        wpp.sendText(msg.phoneNumberId(), msg.fromPhone(),
                "Listo, le avisamos al equipo. Te van a responder por acá lo antes posible 🐾");
        return BotState.HUMAN_HANDOFF;
    }

    // ------- helpers -------

    private boolean isGlobalReset(String input) {
        if (input == null) return false;
        String t = input.trim().toLowerCase();
        return t.equals("menu") || t.equals("menú") || t.equals("/menu") || t.equals("hola");
    }

    private String pickInput(IncomingMessage msg) {
        if (msg.buttonId() != null) return msg.buttonId();
        if (msg.listId() != null) return msg.listId();
        return msg.text();
    }

    private BotState parseState(String s) {
        try { return BotState.valueOf(s); } catch (Exception ex) { return BotState.IDLE; }
    }

    private BotContext loadContext(Conversation conv) {
        if (conv.getContextJson() == null) return new BotContext();
        try {
            return mapper.readValue(conv.getContextJson(), BotContext.class);
        } catch (Exception ex) {
            return new BotContext();
        }
    }

    private String saveContext(BotContext ctx) {
        try {
            return mapper.writeValueAsString(ctx);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String dayNameEs(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "lun";
            case TUESDAY -> "mar";
            case WEDNESDAY -> "mié";
            case THURSDAY -> "jue";
            case FRIDAY -> "vie";
            case SATURDAY -> "sáb";
            case SUNDAY -> "dom";
        };
    }
}
