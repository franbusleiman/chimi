package pet.liro.chimi.api.faq;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.domain.faq.Faq;
import pet.liro.chimi.domain.faq.FaqCategory;
import pet.liro.chimi.security.CurrentUser;
import pet.liro.chimi.service.FaqService;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FaqController {

    private final FaqService service;

    public FaqController(FaqService service) {
        this.service = service;
    }

    @GetMapping
    public List<Faq> list(@RequestParam(value = "category", required = false) FaqCategory category,
                          @RequestParam(value = "active", required = false) Boolean active) {
        Long tid = CurrentUser.require().tenantId();
        if (category != null) return service.listByCategory(tid, category);
        return Boolean.TRUE.equals(active) ? service.listActive(tid) : service.listAll(tid);
    }

    @PostMapping
    public Faq create(@Valid @RequestBody Faq body) {
        return service.create(CurrentUser.require().tenantId(), body);
    }

    @PutMapping("/{id}")
    public Faq update(@PathVariable Long id, @Valid @RequestBody Faq body) {
        return service.update(CurrentUser.require().tenantId(), id, body);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(CurrentUser.require().tenantId(), id);
    }
}
