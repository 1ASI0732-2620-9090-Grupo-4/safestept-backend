package com.safestep.platform.analytics.application.queryservices;

import com.safestep.platform.analytics.domain.model.aggregates.AnalyticsSummary;
import com.safestep.platform.analytics.domain.model.aggregates.Certificate;
import com.safestep.platform.analytics.domain.model.queries.GetCertificatesQuery;
import com.safestep.platform.analytics.domain.model.queries.GetProgressQuery;
import com.safestep.platform.analytics.domain.model.queries.GetSummaryQuery;
import com.safestep.platform.analytics.domain.model.valueobjects.ProgressVisual;
import java.util.*;

public interface AnalyticsQueryService {
    AnalyticsSummary handle(GetSummaryQuery query);
    List<ProgressVisual> handle(GetProgressQuery query);
    List<Certificate> handle(GetCertificatesQuery query);
}
