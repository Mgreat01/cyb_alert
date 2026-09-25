package com.cybAlert.cybAlert.application.risk;

import com.cybAlert.cybAlert.business.risk.RiskService;
import com.cybAlert.cybAlert.business.source.SourceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/risk/sources")
public class RiskController {

    private final RiskService risks;
    private final SourceService sources;

    public RiskController(RiskService risks, SourceService sources) {
        this.risks = risks;
        this.sources = sources;
    }

    @GetMapping("/{id}")
    public SourceRisk findBySource(@PathVariable UUID id) {
        sources.findById(id);
        return new SourceRisk(id, risks.score(id));
    }

    record SourceRisk(UUID sourceId, int score) {
    }
}
