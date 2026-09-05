package com.safestep.platform.analytics.application.internal.queryservices;

import com.safestep.platform.analytics.application.queryservices.AnalyticsQueryService;
import com.safestep.platform.analytics.domain.model.aggregates.AnalyticsSummary;
import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.analytics.domain.model.queries.GetCertificatesQuery;
import com.safestep.platform.analytics.domain.model.queries.GetProgressQuery;
import com.safestep.platform.analytics.domain.model.queries.GetSummaryQuery;
import com.safestep.platform.analytics.domain.model.valueobjects.*;
import com.safestep.platform.analytics.domain.repositories.CertificateRepository;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade;
import com.safestep.platform.simulation.interfaces.acl.SimulationContextFacade;
import org.springframework.stereotype.Service;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.*;

@Service
public class AnalyticsQueryServiceImpl implements AnalyticsQueryService {
    private final SimulationContextFacade simulations;
    private final GamificationContextFacade gamification;
    private final CertificateRepository certificates;

    public AnalyticsQueryServiceImpl(SimulationContextFacade s, GamificationContextFacade g, CertificateRepository c) {
        simulations = s;
        gamification = g;
        certificates = c;
    }

    @Override
    public AnalyticsSummary handle(GetSummaryQuery query) {
        var u = query.username();
        var attempts = simulations.attemptsByUsername(u);
        var progress = gamification.progressByUsername(u);
        int average = attempts.isEmpty() ? 0
                : (int) Math.round(attempts.stream().mapToInt(a -> a.score()).average().orElse(0));
        var metrics = List.of(new Metric("Simulaciones realizadas", String.valueOf(attempts.size()), "Datos reales"),
                new Metric("Precision promedio", average + "%", "Calculado desde intentos"),
                new Metric("XP obtenida", String.valueOf(progress.xp()), "Nivel " + progress.level()),
                new Metric("Racha maxima", progress.streak() + " dias", "Actividad actual"));
        var skills = attempts.stream()
                .collect(Collectors.groupingBy(a -> a.simulationSlug(), Collectors.averagingInt(a -> a.score())))
                .entrySet().stream().map(e -> new SkillProgress(e.getKey(), e.getValue().intValue())).toList();
        var mistakes = attempts.stream().flatMap(a -> a.errors().stream())
                .collect(Collectors.groupingBy(x -> x, Collectors.counting())).entrySet().stream()
                .sorted(Map.Entry.<String, Long> comparingByValue().reversed()).map(e -> new CommonMistake(e.getKey(),
                        e.getValue().intValue(), "Practicar nuevamente el modulo relacionado."))
                .toList();
        var weekly = attempts.stream().filter(a -> a.completedAt() != null)
                .collect(Collectors.groupingBy(a -> a.completedAt().atZone(ZoneId.systemDefault()).getDayOfWeek(),
                        Collectors.counting()))
                .entrySet().stream()
                .map(e -> new WeeklyActivity(e.getKey().getDisplayName(TextStyle.SHORT, new Locale("es")),
                        e.getValue().intValue() * 100, e.getValue().intValue()))
                .toList();
        var performance = List.of(new PerformanceByDifficulty("General", attempts.size(), average));
        return new AnalyticsSummary(metrics, skills, mistakes, weekly, performance);
    }

    @Override
    public List<ProgressVisual> handle(GetProgressQuery query) {
        return simulations.attemptsByUsername(query.username()).stream()
                .collect(Collectors.groupingBy(a -> a.simulationSlug()))
                .entrySet().stream().map(e -> {
                    var values = e.getValue();
                    int best = values.stream().mapToInt(a -> a.score()).max().orElse(0);
                    double avg = values.stream().mapToInt(a -> a.score()).average().orElse(0);
                    var last = values.stream().map(a -> a.completedAt()).filter(Objects::nonNull)
                            .max(Comparator.naturalOrder()).orElse(null);
                    var errors = values.stream().flatMap(a -> a.errors().stream()).distinct().toList();
                    long time = (long) values.stream().mapToLong(a -> a.timeElapsed()).average().orElse(0);
                    return new ProgressVisual(e.getKey(), best, best, avg, values.size(),
                            best >= 80 ? "green" : best >= 50 ? "yellow" : "red", last, errors, time);
                }).toList();
    }

    @Override
    public List<Certificate> handle(GetCertificatesQuery query) {
        return certificates.findByUsername(query.username());
    }
}
