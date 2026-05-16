package pet.liro.chimi.domain.block;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BlockRepository extends JpaRepository<Block, Long> {

    Optional<Block> findByIdAndTenantId(Long id, Long tenantId);

    List<Block> findByTenantIdAndStartAtBetweenOrderByStartAtAsc(
            Long tenantId, Instant from, Instant to);

    @Query("""
        select b from Block b
        where b.tenantId = :tenantId
          and b.startAt < :endAt
          and b.endAt > :startAt
        """)
    List<Block> findOverlapping(@Param("tenantId") Long tenantId,
                                @Param("startAt") Instant startAt,
                                @Param("endAt") Instant endAt);
}
