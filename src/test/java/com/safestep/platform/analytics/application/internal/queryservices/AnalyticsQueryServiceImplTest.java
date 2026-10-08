package com.safestep.platform.analytics.application.internal.queryservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.analytics.domain.model.queries.GetCertificatesQuery;
import com.safestep.platform.analytics.domain.model.queries.GetProgressQuery;
import com.safestep.platform.analytics.domain.model.queries.GetSummaryQuery;
import com.safestep.platform.analytics.domain.repositories.CertificateRepository;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade;
import com.safestep.platform.gamification.interfaces.acl.GamificationContextFacade.ProgressSnapshot;
import com.safestep.platform.simulation.interfaces.acl.SimulationContextFacade;
import com.safestep.platform.simulation.interfaces.acl.SimulationContextFacade.AttemptSnapshot;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsQueryServiceImplTest {

    private static final Instant MONDAY = Instant.parse("2026-09-14T15:00:00Z");
    private static final Instant TUESDAY = Instant.parse("2026-09-15T15:00:00Z");

    @Mock
    private SimulationContextFacade simulations;
    @Mock
    private GamificationContextFacade gamification;
    @Mock
    private CertificateRepository certificates;

    @InjectMocks
    private AnalyticsQueryServiceImpl service;

    private AttemptSnapshot attempt(String slug, int score, long elapsed, Instant at, String... errors) {
        return new AttemptSnapshot("att-" + slug + score, slug, score, 10, score / 10, elapsed, at,
                List.of(errors));
    }

    @Test
    @DisplayName("handle(GetSummaryQuery) should build metrics, skills and mistakes from real attempts (AAA)")
    void handleSummary_WithAttempts_BuildsAggregates() {
        // Arrange
        when(simulations.attemptsByUsername("ana")).thenReturn(List.of(
                attempt("cpr", 80, 60, MONDAY, "wrong-rhythm", "late-call"),
                attempt("cpr", 60, 90, TUESDAY, "wrong-rhythm"),
                attempt("burns", 100, 30, null)));
        when(gamification.progressByUsername("ana")).thenReturn(new ProgressSnapshot(3, 2400, 150, 4, 3));

        // Act
        var summary = service.handle(new GetSummaryQuery("ana"));

        // Assert
        assertEquals(4, summary.getSummary().size());
        assertEquals("3", summary.getSummary().get(0).value());
        assertEquals("80%", summary.getSummary().get(1).value());
        assertEquals("2400", summary.getSummary().get(2).value());
        assertEquals("Nivel 3", summary.getSummary().get(2).trend());
        assertEquals("4 dias", summary.getSummary().get(3).value());
        assertEquals(2, summary.getSkillProgress().size());
        assertEquals("wrong-rhythm", summary.getCommonMistakes().get(0).topic());
        assertEquals(2, summary.getCommonMistakes().get(0).mistakes());
        assertEquals(2, summary.getWeeklyActivity().size());
        assertEquals(3, summary.getPerformanceByDifficulty().get(0).completed());
        assertEquals(80, summary.getPerformanceByDifficulty().get(0).accuracy());
    }

    @Test
    @DisplayName("handle(GetSummaryQuery) should return zeroed metrics when the user has no attempts (AAA)")
    void handleSummary_WithoutAttempts_ReturnsZeroedMetrics() {
        // Arrange
        when(simulations.attemptsByUsername("new-user")).thenReturn(List.of());
        when(gamification.progressByUsername("new-user")).thenReturn(new ProgressSnapshot(1, 0, 0, 0, 0));

        // Act
        var summary = service.handle(new GetSummaryQuery("new-user"));

        // Assert
        assertEquals("0", summary.getSummary().get(0).value());
        assertEquals("0%", summary.getSummary().get(1).value());
        assertTrue(summary.getSkillProgress().isEmpty());
        assertTrue(summary.getCommonMistakes().isEmpty());
        assertTrue(summary.getWeeklyActivity().isEmpty());
        assertEquals(0, summary.getPerformanceByDifficulty().get(0).accuracy());
    }

    @Test
    @DisplayName("handle(GetProgressQuery) should classify each simulation by its best score (AAA)")
    void handleProgress_ClassifiesByBestScore() {
        // Arrange
        when(simulations.attemptsByUsername("ana")).thenReturn(List.of(
                attempt("cpr", 90, 60, MONDAY, "late-call"),
                attempt("cpr", 70, 120, TUESDAY, "late-call", "wrong-rhythm"),
                attempt("burns", 55, 30, MONDAY),
                attempt("choking", 20, 45, null)));

        // Act
        var progress = service.handle(new GetProgressQuery("ana"));

        // Assert
        assertEquals(3, progress.size());
        var cpr = progress.stream().filter(p -> p.simulationId().equals("cpr")).findFirst().orElseThrow();
        assertEquals(90, cpr.bestScore());
        assertEquals(80.0, cpr.averageScore());
        assertEquals(2, cpr.totalAttempts());
        assertEquals("green", cpr.statusColor());
        assertEquals(TUESDAY, cpr.lastPracticedAt());
        assertEquals(List.of("late-call", "wrong-rhythm"), cpr.commonErrors());
        assertEquals(90L, cpr.averageResponseTime());
        var burns = progress.stream().filter(p -> p.simulationId().equals("burns")).findFirst().orElseThrow();
        assertEquals("yellow", burns.statusColor());
        var choking = progress.stream().filter(p -> p.simulationId().equals("choking")).findFirst().orElseThrow();
        assertEquals("red", choking.statusColor());
        assertNull(choking.lastPracticedAt());
    }

    @Test
    @DisplayName("handle(GetCertificatesQuery) should delegate to the certificate repository (AAA)")
    void handleCertificates_DelegatesToRepository() {
        // Arrange
        var certificate = new Certificate(1L, "ana", "CPR", 95, "GOLD", MONDAY, "CODE-1", null, null);
        when(certificates.findByUsername("ana")).thenReturn(List.of(certificate));

        // Act
        var result = service.handle(new GetCertificatesQuery("ana"));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(certificate, result.get(0));
        verify(certificates).findByUsername("ana");
    }
}
