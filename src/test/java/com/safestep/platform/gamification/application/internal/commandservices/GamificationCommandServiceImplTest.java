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

    @Test
    void createMissionSavesValidMission() {
        var missions = mock(MissionRepository.class);
        var service = new GamificationCommandServiceImpl(missions, mock(BadgeRepository.class));
        var mission = mission("mission-1", "Practice");
        when(missions.existsByExternalId("mission-1")).thenReturn(false);
        when(missions.save(mission)).thenReturn(mission);

        var result = service.handle(new CreateMissionCommand(mission));

        assertTrue(result.isSuccess());
        verify(missions).save(mission);
    }

    @Test
    void createMissionRequiresAnExternalId() {
        var service = new GamificationCommandServiceImpl(mock(MissionRepository.class), mock(BadgeRepository.class));

        var result = service.handle(new CreateMissionCommand(mission(" ", "Practice")));

        assertTrue(result.isFailure());
    }

    @Test
    void createMissionRejectsNegativeRewardsAndGoals() {
        var missions = mock(MissionRepository.class);
        var service = new GamificationCommandServiceImpl(missions, mock(BadgeRepository.class));

        var negativeGoal = new Mission(null, "m-1", "T", MissionCadence.DAILY, -1, 10, 10, "active", "i", "u");
        var negativeXp = new Mission(null, "m-2", "T", MissionCadence.DAILY, 1, -10, 10, "active", "i", "u");
        var negativeCoins = new Mission(null, "m-3", "T", MissionCadence.DAILY, 1, 10, -10, "active", "i", "u");

        assertTrue(service.handle(new CreateMissionCommand(negativeGoal)).isFailure());
        assertTrue(service.handle(new CreateMissionCommand(negativeXp)).isFailure());
        assertTrue(service.handle(new CreateMissionCommand(negativeCoins)).isFailure());
        verify(missions, never()).save(any());
    }

    @Test
    void updateMissionFailsWhenMissionDoesNotExist() {
        var missions = mock(MissionRepository.class);
        var service = new GamificationCommandServiceImpl(missions, mock(BadgeRepository.class));
        when(missions.findByExternalId("ghost")).thenReturn(Optional.empty());

        assertTrue(service.handle(new UpdateMissionCommand("ghost", mission("ghost", "T"))).isFailure());
        assertTrue(service.handle(new UpdateMissionCommand(" ", mission("x", "T"))).isFailure());
    }

    @Test
    void updateMissionRejectsInvalidRewards() {
        var missions = mock(MissionRepository.class);
        var service = new GamificationCommandServiceImpl(missions, mock(BadgeRepository.class));
        when(missions.findByExternalId("m-1")).thenReturn(Optional.of(missionWithId(1L, "m-1", "Old")));
        var invalid = new Mission(null, "m-1", "T", MissionCadence.DAILY, -5, 10, 10, "active", "i", "u");

        assertTrue(service.handle(new UpdateMissionCommand("m-1", invalid)).isFailure());
        verify(missions, never()).save(any());
    }

    @Test
    void deleteMissionRemovesExistingMission() {
        var missions = mock(MissionRepository.class);
        var service = new GamificationCommandServiceImpl(missions, mock(BadgeRepository.class));
        var existing = missionWithId(1L, "m-1", "Old");
        when(missions.findByExternalId("m-1")).thenReturn(Optional.of(existing));
        when(missions.findByExternalId("ghost")).thenReturn(Optional.empty());

        assertTrue(service.handle(new com.safestep.platform.gamification.domain.model.commands.DeleteMissionCommand("m-1"))
                .isSuccess());
        assertTrue(service.handle(new com.safestep.platform.gamification.domain.model.commands.DeleteMissionCommand("ghost"))
                .isFailure());
        verify(missions).delete(existing);
    }

    @Test
    void createBadgeSavesNewBadgeAndRequiresAnId() {
        var badges = mock(BadgeRepository.class);
        var service = new GamificationCommandServiceImpl(mock(MissionRepository.class), badges);
        var badge = badge("badge-1", "Starter");
        when(badges.existsByExternalId("badge-1")).thenReturn(false);
        when(badges.save(badge)).thenReturn(badge);

        assertTrue(service.handle(new CreateBadgeCommand(badge)).isSuccess());
        assertTrue(service.handle(new CreateBadgeCommand(badge(" ", "NoId"))).isFailure());
    }

    @Test
    void updateBadgeKeepsDatabaseIdAndHandlesMissingBadge() {
        var badges = mock(BadgeRepository.class);
        var service = new GamificationCommandServiceImpl(mock(MissionRepository.class), badges);
        var existing = new Badge(4L, "badge-1", "Old", BadgeRarity.RARE, "d", "r");
        when(badges.findByExternalId("badge-1")).thenReturn(Optional.of(existing));
        when(badges.findByExternalId("ghost")).thenReturn(Optional.empty());
        when(badges.save(any(Badge.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var updated = service.handle(new com.safestep.platform.gamification.domain.model.commands.UpdateBadgeCommand(
                "badge-1", badge("ignored", "Renamed")));
        var missing = service.handle(new com.safestep.platform.gamification.domain.model.commands.UpdateBadgeCommand(
                "ghost", badge("ghost", "Renamed")));
        var blank = service.handle(new com.safestep.platform.gamification.domain.model.commands.UpdateBadgeCommand(
                null, badge("x", "Renamed")));

        assertTrue(updated.isSuccess());
        assertTrue(missing.isFailure());
        assertTrue(blank.isFailure());
        var saved = ArgumentCaptor.forClass(Badge.class);
        verify(badges).save(saved.capture());
        assertEquals(4L, saved.getValue().getId());
        assertEquals("badge-1", saved.getValue().getExternalId());
        assertEquals("Renamed", saved.getValue().getName());
    }

    @Test
    void deleteBadgeRemovesExistingBadge() {
        var badges = mock(BadgeRepository.class);
        var service = new GamificationCommandServiceImpl(mock(MissionRepository.class), badges);
        var existing = new Badge(4L, "badge-1", "Old", BadgeRarity.RARE, "d", "r");
        when(badges.findByExternalId("badge-1")).thenReturn(Optional.of(existing));

        assertTrue(service.handle(new DeleteBadgeCommand("badge-1")).isSuccess());
        verify(badges).delete(existing);
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
