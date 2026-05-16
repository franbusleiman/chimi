package pet.liro.chimi.api.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pet.liro.chimi.domain.appointment.AppointmentSource;

import java.math.BigDecimal;
import java.time.Instant;

public record AppointmentCreateRequest(
        @NotNull Long appointmentTypeId,
        @NotNull Instant startAt,
        @NotBlank String tutorFirstName,
        @NotBlank String tutorLastName,
        @NotBlank String tutorPhone,
        @NotBlank String petFirstName,
        String petLastName,
        String petSpecies,
        AppointmentSource source,
        String notes,
        Boolean prepaid,
        BigDecimal prepaidAmount
) {}
