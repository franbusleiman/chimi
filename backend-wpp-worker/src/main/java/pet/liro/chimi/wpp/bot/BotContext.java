package pet.liro.chimi.wpp.bot;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

public class BotContext implements Serializable {
    public Long appointmentTypeId;
    public String appointmentTypeName;
    public Integer durationMinutes;
    public LocalDate selectedDate;
    public Instant selectedSlot;
    public String tutorFirstName;
    public String tutorLastName;
    public String petFirstName;
    public String petLastName;
}
