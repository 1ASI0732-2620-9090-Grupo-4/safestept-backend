package com.safestep.platform.gamification.application.internal.commandservices;

import com.safestep.platform.gamification.domain.model.aggregates.Badge;
import com.safestep.platform.gamification.domain.model.aggregates.Mission;
import com.safestep.platform.gamification.domain.model.commands.CreateBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.CreateMissionCommand;
import com.safestep.platform.gamification.domain.model.commands.DeleteBadgeCommand;
import com.safestep.platform.gamification.domain.model.commands.UpdateMissionCommand;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.BadgeRarity;
import com.safestep.platform.gamification.domain.model.valueobjects.GamificationValueObjects.MissionCadence;
import com.safestep.platform.gamification.domain.repositories.BadgeRepository;
import com.safestep.platform.gamification.domain.repositories.MissionRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GamificationCommandServiceImplTest {

    @Test
    void createMissionRejectsDuplicatedExternalId() {
        var missions = mock(MissionRepository.class);
        var service = new GamificationCommandServiceImpl(missions, mock(BadgeRepository.class));
        when(missions.existsByExternalId("mission-1")).thenReturn(true);

        var result = service.handle(new CreateMissionCommand(mission("mission-1", "Practice")));

        assertTrue(result.isFailure());
        verify(missions, never()).save(any());
    }

    @Test
    void updateMissionKeepsDatabaseIdAndPathExternalId() {
        var missions = mock(MissionRepository.class);
        var service = new GamificationCommandServiceImpl(missions, mock(BadgeRepository.class));
        when(missions.findByExternalId("mission-1")).thenReturn(Optional.of(missionWithId(7L, "mission-1", "Old")));
        when(missions.save(any(Mission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = service.handle(new UpdateMissionCommand("mission-1", mission("ignored", "Updated")));

        assertTrue(result.isSuccess());
        var saved = ArgumentCaptor.forClass(Mission.class);
        verify(missions).save(saved.capture());
        assertEquals(7L, saved.getValue().getId());
        assertEquals("mission-1", saved.getValue().getExternalId());
        assertEquals("Updated", saved.getValue().getTitle());
    }

    @Test
    void createBadgeRejectsDuplicatedExternalId() {
        var badges = mock(BadgeRepository.class);
        var service = new GamificationCommandServiceImpl(mock(MissionRepository.class), badges);
        when(badges.existsByExternalId("badge-1")).thenReturn(true);

        var result = service.handle(new CreateBadgeCommand(badge("badge-1", "Starter")));

        assertTrue(result.isFailure());
        verify(badges, never()).save(any());
    }

    @Test
    void deleteBadgeReturnsNotFoundWhenMissing() {
        var badges = mock(BadgeRepository.class);
        var service = new GamificationCommandServiceImpl(mock(MissionRepository.class), badges);
        when(badges.findByExternalId("missing")).thenReturn(Optional.empty());

        var result = service.handle(new DeleteBadgeCommand("missing"));

        assertTrue(result.isFailure());
        verify(badges, never()).delete(any());
    }

    private Mission mission(String externalId, String title) {
        return missionWithId(null, externalId, title);
    }

    private Mission missionWithId(Long id, String externalId, String title) {
        return new Mission(id, externalId, title, MissionCadence.DAILY, 3, 40, 10, "active",
                "Complete practice", "Available");
    }

    private Badge badge(String externalId, String name) {
        return new Badge(null, externalId, name, BadgeRarity.COMMON, "Badge description", "Complete one mission");
    }
}
