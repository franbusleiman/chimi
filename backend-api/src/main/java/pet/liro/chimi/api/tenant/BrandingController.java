package pet.liro.chimi.api.tenant;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pet.liro.chimi.domain.tenant.Branding;
import pet.liro.chimi.security.CurrentUser;
import pet.liro.chimi.service.BrandingService;

@RestController
@RequestMapping("/api/branding")
public class BrandingController {

    private final BrandingService service;

    public BrandingController(BrandingService service) {
        this.service = service;
    }

    @GetMapping
    public Branding get() {
        return service.get(CurrentUser.require().tenantId());
    }

    @PutMapping
    public Branding put(@Valid @RequestBody Branding body) {
        return service.upsert(CurrentUser.require().tenantId(), body);
    }
}
