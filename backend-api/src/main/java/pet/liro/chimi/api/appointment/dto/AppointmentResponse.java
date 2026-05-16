package pet.liro.chimi.api.appointment.dto;

import pet.liro.chimi.domain.appointment.Appointment;
import pet.liro.chimi.domain.appointment.AppointmentSource;
import pet.liro.chimi.domain.appointment.AppointmentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record AppointmentResponse(
        Long id,
        Long appointmentTypeId,
        String appointmentTypeName,
        Long petId,
        String petName,
        Long tutorId,
        String tutorName,
        String tutorPhone,
        Instant startAt,
        Instant endAt,
        AppointmentStatus status,
        AppointmentSource source,
        String notes,
        Boolean prepaid,
        BigDecimal prepaidAmount,
        Instant createdAt
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getAppointmentType().getId(),
                a.getAppointmentType().getName(),
                a.getPet().getId(),
                (a.getPet().getFirstName() + (a.getPet().getLastName() != null ? " " + a.getPet().getLastName() : "")).trim(),
                a.getTutor().getId(),
                (a.getTutor().getFirstName() + " " + a.getTutor().getLastName()).trim(),
                a.getTutor().getPhone(),
                a.getStartAt(),
                a.getEndAt(),
                a.getStatus(),
                a.getSource(),
                a.getNotes(),
                a.getPrepaid(),
                a.getPrepaidAmount(),
                a.getCreatedAt()
        );
    }
}
