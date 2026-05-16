package pet.liro.chimi.domain.pet;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetRepository extends JpaRepository<Pet, Long> {
    List<Pet> findByTenantIdAndTutorId(Long tenantId, Long tutorId);
    List<Pet> findByTenantId(Long tenantId);
}
