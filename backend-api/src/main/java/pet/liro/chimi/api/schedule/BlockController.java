package pet.liro.chimi.api.schedule;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.domain.block.Block;
import pet.liro.chimi.security.CurrentUser;
import pet.liro.chimi.service.BlockService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/blocks")
public class BlockController {

    private final BlockService service;

    public BlockController(BlockService service) {
        this.service = service;
    }

    @GetMapping
    public List<Block> list(@RequestParam("from") Instant from, @RequestParam("to") Instant to) {
        return service.listBetween(CurrentUser.require().tenantId(), from, to);
    }

    @PostMapping
    public Block create(@Valid @RequestBody Block body) {
        var user = CurrentUser.require();
        return service.create(user.tenantId(), user.userId(), body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(CurrentUser.require().tenantId(), id);
    }
}
