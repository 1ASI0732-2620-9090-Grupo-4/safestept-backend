package com.safestep.platform.analytics.domain.model.aggregates;

import com.safestep.platform.analytics.domain.model.valueobjects.*;
import java.util.List;

public class AnalyticsSummary extends com.safestep.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot<AnalyticsSummary> {
    private List<Metric> summary;
    private List<SkillProgress> skillProgress;
    private List<CommonMistake> commonMistakes;
    private List<WeeklyActivity> weeklyActivity;
    private List<PerformanceByDifficulty> performanceByDifficulty;

    public AnalyticsSummary() {}

    public AnalyticsSummary(List<Metric> summary, List<SkillProgress> skillProgress,
            List<CommonMistake> commonMistakes, List<WeeklyActivity> weeklyActivity,
            List<PerformanceByDifficulty> performanceByDifficulty) {
        this.summary = summary;
        this.skillProgress = skillProgress;
        this.commonMistakes = commonMistakes;
        this.weeklyActivity = weeklyActivity;
        this.performanceByDifficulty = performanceByDifficulty;
    }

    public List<Metric> getSummary() { return summary; }
    public List<SkillProgress> getSkillProgress() { return skillProgress; }
    public List<CommonMistake> getCommonMistakes() { return commonMistakes; }
    public List<WeeklyActivity> getWeeklyActivity() { return weeklyActivity; }
    public List<PerformanceByDifficulty> getPerformanceByDifficulty() { return performanceByDifficulty; }
}
