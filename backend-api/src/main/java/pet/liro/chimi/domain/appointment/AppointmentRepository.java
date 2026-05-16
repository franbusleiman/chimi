package pet.liro.chimi.domain.appointment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    Optional<Appointment> findByIdAndTenantId(Long id, Long tenantId);

    List<Appointment> findByTenantIdAndStartAtBetweenOrderByStartAtAsc(
            Long tenantId, Instant from, Instant to);

    @Query("""
        select a from Appointment a
        where a.tenantId = :tenantId
          and a.status <> pet.liro.chimi.domain.appointment.AppointmentStatus.CANCELLED
          and a.startAt < :endAt
          and a.endAt > :startAt
        """)
    List<Appointment> findOverlapping(@Param("tenantId") Long tenantId,
                                      @Param("startAt") Instant startAt,
                                      @Param("endAt") Instant endAt);

    @Query("""
        select count(a) from Appointment a
        where a.tenantId = :tenantId
          and a.status <> pet.liro.chimi.domain.appointment.AppointmentStatus.CANCELLED
          and a.startAt < :endAt
          and a.endAt > :startAt
        """)
    long countOverlapping(@Param("tenantId") Long tenantId,
                          @Param("startAt") Instant startAt,
                          @Param("endAt") Instant endAt);

    List<Appointment> findByTenantIdAndTutorIdOrderByStartAtDesc(Long tenantId, Long tutorId);
}
