package com.safestep.platform.analytics.application.internal.commandservices;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.analytics.domain.repositories.CertificateRepository;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CertificateCommandServiceImplTest {

    private CertificateRepository repository;
    private CertificateCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        repository = mock(CertificateRepository.class);
        service = new CertificateCommandServiceImpl(repository);
        when(repository.save(any(Certificate.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ApplicationError errorOf(Result<?, ApplicationError> result) {
        return ((Result.Failure<?, ApplicationError>) result).error();
    }

    @Test
    @DisplayName("issue should refuse scores below 80 (AAA)")
    void issue_LowScore_IsRefused() {
        // Act
        var result = service.issue("ana", "cpr", "CPR", 79);

        // Assert
        assertEquals("BUSINESS_RULE_VIOLATION", errorOf(result).code());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("issue should not issue the same certificate twice (AAA)")
    void issue_DuplicateCode_ReturnsConflict() {
        // Arrange
        when(repository.existsByVerificationCode("CERT-SS-ANA-CPR")).thenReturn(true);

        // Act
        var result = service.issue("ana", "cpr", "CPR", 90);

        // Assert
        assertEquals("CERTIFICATE_CONFLICT", errorOf(result).code());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("issue should grade the achievement level from the score (AAA)")
    void issue_GradesAchievementLevel() {
        // Act
        var basic = service.issue("ana", "cpr", "CPR", 80).toOptional().orElseThrow();
        var intermediate = service.issue("ana", "burns", "Burns", 90).toOptional().orElseThrow();
        var advanced = service.issue("ana", "choking", "Choking", 95).toOptional().orElseThrow();

        // Assert
        assertEquals("Basico", basic.getAchievementLevel());
        assertEquals("Intermedio", intermediate.getAchievementLevel());
        assertEquals("Avanzado", advanced.getAchievementLevel());
    }

    @Test
    @DisplayName("issue should build a sanitized verification code and public urls (AAA)")
    void issue_BuildsVerificationCodeAndUrls() {
        // Act
        var certificate = service.issue("ana.torres", "cpr adult", "CPR", 92).toOptional().orElseThrow();

        // Assert
        assertEquals("CERT-SS-ANA-TORRES-CPR-ADULT", certificate.getVerificationCode());
        assertTrue(certificate.getQrCodeUrl().endsWith("/verify/CERT-SS-ANA-TORRES-CPR-ADULT"));
        assertTrue(certificate.getDownloadablePdfUrl().endsWith("CERT-SS-ANA-TORRES-CPR-ADULT.pdf"));
        assertEquals("ana.torres", certificate.getUsername());
        assertEquals(92, certificate.getScore());
    }
}
