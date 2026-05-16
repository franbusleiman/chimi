package pet.liro.chimi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pet.liro.chimi.api.appointment.dto.AppointmentCreateRequest;
import pet.liro.chimi.api.common.NotFoundException;
import pet.liro.chimi.domain.appointment.*;
import pet.liro.chimi.domain.block.BlockRepository;
import pet.liro.chimi.domain.pet.Pet;
import pet.liro.chimi.domain.pet.PetRepository;
import pet.liro.chimi.domain.tenant.Tenant;
import pet.liro.chimi.domain.tenant.TenantRepository;
import pet.liro.chimi.domain.tutor.Tutor;
import pet.liro.chimi.domain.tutor.TutorRepository;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class AppointmentService {

    private final AppointmentRepository appointments;
    private final AppointmentTypeRepository types;
    private final BlockRepository blocks;
    private final TutorRepository tutors;
    private final PetRepository pets;
    private final TenantRepository tenantsRepo;

    public AppointmentService(AppointmentRepository appointments,
                              AppointmentTypeRepository types,
                              BlockRepository blocks,
                              TutorRepository tutors,
                              PetRepository pets,
                              TenantRepository tenantsRepo) {
        this.appointments = appointments;
        this.types = types;
        this.blocks = blocks;
        this.tutors = tutors;
        this.pets = pets;
        this.tenantsRepo = tenantsRepo;
    }

    public List<Appointment> listByDate(Long tenantId, LocalDate date) {
        Tenant t = tenantsRepo.findById(tenantId).orElseThrow(() -> new NotFoundException("Tenant"));
        ZoneId zone = ZoneId.of(t.getTimezone());
        Instant from = date.atStartOfDay(zone).toInstant();
        Instant to = date.plusDays(1).atStartOfDay(zone).toInstant();
        return appointments.findByTenantIdAndStartAtBetweenOrderByStartAtAsc(tenantId, from, to);
    }

    public List<Appointment> listBetween(Long tenantId, Instant from, Instant to) {
        return appointments.findByTenantIdAndStartAtBetweenOrderByStartAtAsc(tenantId, from, to);
    }

    public Appointment get(Long tenantId, Long id) {
        return appointments.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Turno no encontrado"));
    }

    @Transactional
    public Appointment create(Long tenantId, AppointmentCreateRequest req, AppointmentSource defaultSource) {
        Tenant tenant = tenantsRepo.findById(tenantId)
                .orElseThrow(() -> new NotFoundException("Tenant"));
        AppointmentType type = types.findByIdAndTenantId(req.appointmentTypeId(), tenantId)
                .orElseThrow(() -> new NotFoundException("Tipo de turno"));
        if (!Boolean.TRUE.equals(type.getActive())) {
            throw new ResponseStatusException(BAD_REQUEST, "Tipo de turno inactivo");
        }

        Instant start = req.startAt();
        Instant end = start.plus(Duration.ofMinutes(type.getDurationMinutes()));

        if (!blocks.findOverlapping(tenantId, start, end).isEmpty()) {
            throw new ResponseStatusException(CONFLICT, "El horario está bloqueado");
        }
        long busy = appointments.countOverlapping(tenantId, start, end);
        if (busy >= tenant.getParallelSlots()) {
            throw new ResponseStatusException(CONFLICT, "No hay disponibilidad en ese horario");
        }

        Tutor tutor = tutors.findByTenantIdAndPhone(tenantId, req.tutorPhone())
                .orElseGet(() -> {
                    Tutor t = new Tutor();
                    t.setTenantId(tenantId);
                    t.setPhone(req.tutorPhone());
                    return t;
                });
        tutor.setFirstName(req.tutorFirstName());
        tutor.setLastName(req.tutorLastName());
        tutor = tutors.save(tutor);

        Pet pet = new Pet();
        pet.setTenantId(tenantId);
        pet.setFirstName(req.petFirstName());
        pet.setLastName(req.petLastName());
        pet.setSpecies(req.petSpecies());
        pet.setTutor(tutor);
        pet = pets.save(pet);

        Appointment a = new Appointment();
        a.setTenantId(tenantId);
        a.setAppointmentType(type);
        a.setTutor(tutor);
        a.setPet(pet);
        a.setStartAt(start);
        a.setEndAt(end);
        a.setStatus(AppointmentStatus.SCHEDULED);
        a.setSource(req.source() != null ? req.source() : defaultSource);
        a.setNotes(req.notes());
        a.setPrepaid(Boolean.TRUE.equals(req.prepaid()));
        a.setPrepaidAmount(req.prepaidAmount());
        return appointments.save(a);
    }

    @Transactional
    public Appointment reschedule(Long tenantId, Long id, Instant newStart) {
        Appointment a = get(tenantId, id);
        if (a.getStatus() == AppointmentStatus.CANCELLED) {
            throw new ResponseStatusException(BAD_REQUEST, "Turno cancelado, no se puede reprogramar");
        }
        Instant newEnd = newStart.plus(Duration.ofMinutes(a.getAppointmentType().getDurationMinutes()));
        Tenant tenant = tenantsRepo.findById(tenantId).orElseThrow();
        if (!blocks.findOverlapping(tenantId, newStart, newEnd).isEmpty()) {
            throw new ResponseStatusException(CONFLICT, "El horario está bloqueado");
        }
        long busy = appointments.findOverlapping(tenantId, newStart, newEnd).stream()
                .filter(other -> !other.getId().equals(a.getId()))
                .count();
        if (busy >= tenant.getParallelSlots()) {
            throw new ResponseStatusException(CONFLICT, "No hay disponibilidad en ese horario");
        }
        a.setStartAt(newStart);
        a.setEndAt(newEnd);
        return appointments.save(a);
    }

    @Transactional
    public Appointment cancel(Long tenantId, Long id, String reason) {
        Appointment a = get(tenantId, id);
        a.setStatus(AppointmentStatus.CANCELLED);
        a.setCancelledAt(Instant.now());
        a.setCancelReason(reason);
        return appointments.save(a);
    }

    @Transactional
    public Appointment markAttended(Long tenantId, Long id) {
        Appointment a = get(tenantId, id);
        a.setStatus(AppointmentStatus.ATTENDED);
        return appointments.save(a);
    }

    @Transactional
    public Appointment markNoShow(Long tenantId, Long id) {
        Appointment a = get(tenantId, id);
        a.setStatus(AppointmentStatus.NO_SHOW);
        return appointments.save(a);
    }
}
