package pet.liro.chimi.wpp.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.wpp.bot.BotEngine;
import pet.liro.chimi.wpp.config.WppProperties;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WppProperties props;
    private final BotEngine bot;

    public WebhookController(WppProperties props, BotEngine bot) {
        this.props = props;
        this.bot = bot;
    }

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {
        if ("subscribe".equals(mode) && props.verifyToken().equals(token)) {
            return ResponseEntity.ok(challenge);
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("verification failed");
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody JsonNode payload) {
        try {
            bot.handleIncoming(payload);
        } catch (Exception ex) {
            log.error("Error procesando webhook: {}", ex.getMessage(), ex);
        }
        // siempre 200 para que Meta no reintente en loop
        return ResponseEntity.ok().build();
    }
}
