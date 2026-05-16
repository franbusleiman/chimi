package pet.liro.chimi.wpp.bot;

public record IncomingMessage(
        String phoneNumberId,
        String fromPhone,
        String messageId,
        String type,
        String text,
        String buttonId,
        String listId
) {}
