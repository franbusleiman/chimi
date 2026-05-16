package pet.liro.chimi.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pet.liro.chimi.api.common.NotFoundException;
import pet.liro.chimi.domain.block.Block;
import pet.liro.chimi.domain.block.BlockRepository;

import java.time.Instant;
import java.util.List;

@Service
public class BlockService {

    private final BlockRepository repo;

    public BlockService(BlockRepository repo) {
        this.repo = repo;
    }

    public List<Block> listBetween(Long tenantId, Instant from, Instant to) {
        return repo.findByTenantIdAndStartAtBetweenOrderByStartAtAsc(tenantId, from, to);
    }

    @Transactional
    public Block create(Long tenantId, Long userId, Block input) {
        input.setTenantId(tenantId);
        input.setCreatedByUserId(userId);
        return repo.save(input);
    }

    @Transactional
    public void delete(Long tenantId, Long id) {
        Block b = repo.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Bloqueo no encontrado"));
        repo.delete(b);
    }
}
