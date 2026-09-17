package com.safestep.platform.gamification.interfaces.rest;

import com.safestep.platform.gamification.application.commandservices.GamificationCommandService;
import com.safestep.platform.shared.application.services.CurrentUserService;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.safestep.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.safestep.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.safestep.platform.gamification.domain.model.commands.*;
import com.safestep.platform.gamification.application.queryservices.GamificationQueryService;
import com.safestep.platform.gamification.domain.model.queries.*;
import com.safestep.platform.gamification.interfaces.rest.resources.BadgeResource;
import com.safestep.platform.gamification.interfaces.rest.resources.MissionResource;
import com.safestep.platform.gamification.interfaces.rest.transform.GamificationResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/gamification", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Gamification", description = "XP, coins, missions, badges and leaderboard endpoints")
public class GamificationController {

    private final GamificationQueryService gamificationQueryService;
    private final GamificationCommandService gamificationCommandService;
    private final CurrentUserService currentUserService;

    public GamificationController(GamificationQueryService gamificationQueryService,
            GamificationCommandService gamificationCommandService,
            CurrentUserService currentUserService) {
        this.gamificationQueryService = gamificationQueryService;
        this.gamificationCommandService = gamificationCommandService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/summary/me")
    @Operation(summary = "Get current user gamification summary")
    public ResponseEntity<?> getMySummary() {
        return ResponseEntity.ok(GamificationResourceAssembler
                .toResource(gamificationQueryService.handle(new GetSummaryQuery(currentUserService.username()))));
    }

    @GetMapping("/missions")
    @Operation(summary = "Get available missions")
    public ResponseEntity<?> getMissions() {
        var username = currentUserService.username();
        return ResponseEntity.ok(gamificationQueryService.handle(new GetMissionsQuery()).stream()
                .map(m -> GamificationResourceAssembler.toResource(m,
                        gamificationQueryService.handle(new GetMissionProgressQuery(username, m.getExternalId()))))
                .toList());
    }

    @PostMapping("/missions")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create mission")
    public ResponseEntity<?> createMission(@Valid @RequestBody MissionResource payload) {
        var result = gamificationCommandService
                .handle(new CreateMissionCommand(GamificationResourceAssembler.toMission(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                mission -> GamificationResourceAssembler.toResource(mission, 0), HttpStatus.CREATED);
    }

    @PutMapping("/missions/{missionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update mission")
    public ResponseEntity<?> updateMission(@PathVariable String missionId,
            @Valid @RequestBody MissionResource payload) {
        var result = gamificationCommandService
                .handle(new UpdateMissionCommand(missionId, GamificationResourceAssembler.toMission(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                mission -> GamificationResourceAssembler.toResource(mission, 0), HttpStatus.OK);
    }

    @DeleteMapping({ "/missions/{missionId}", "/missions/" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete mission")
    public ResponseEntity<?> deleteMission(@PathVariable(required = false) String missionId) {
        return noContentFromResult(gamificationCommandService.handle(new DeleteMissionCommand(missionId == null ? "" : missionId)));
    }

    @GetMapping("/badges/me")
    @Operation(summary = "Get current user badges")
    public ResponseEntity<?> getMyBadges() {
        var username = currentUserService.username();
        var unlocked = gamificationQueryService.handle(new GetUnlockedBadgeIdsQuery(username));
        return ResponseEntity.ok(gamificationQueryService.handle(new GetBadgesQuery(username)).stream()
                .map(b -> GamificationResourceAssembler.toResource(b, unlocked.contains(b.getExternalId()))).toList());
    }

    @PostMapping({ "/badges", "/badges/me" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Create badge")
    public ResponseEntity<?> createBadge(@Valid @RequestBody BadgeResource payload) {
        var result = gamificationCommandService
                .handle(new CreateBadgeCommand(GamificationResourceAssembler.toBadge(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                badge -> GamificationResourceAssembler.toResource(badge, false), HttpStatus.CREATED);
    }

    @PutMapping({ "/badges/{badgeId}", "/badges/me/{badgeId}" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Update badge")
    public ResponseEntity<?> updateBadge(@PathVariable String badgeId, @Valid @RequestBody BadgeResource payload) {
        var result = gamificationCommandService
                .handle(new UpdateBadgeCommand(badgeId, GamificationResourceAssembler.toBadge(payload)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result,
                badge -> GamificationResourceAssembler.toResource(badge, false), HttpStatus.OK);
    }

    @DeleteMapping({ "/badges/{badgeId}", "/badges/me/{badgeId}", "/badges/", "/badges/me/" })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Delete badge")
    public ResponseEntity<?> deleteBadge(@PathVariable(required = false) String badgeId) {
        return noContentFromResult(gamificationCommandService.handle(new DeleteBadgeCommand(badgeId == null ? "" : badgeId)));
    }

    @GetMapping("/leaderboard")
    @Operation(summary = "Get SafeStep leaderboard")
    public ResponseEntity<?> getLeaderboard() {
        var players = gamificationQueryService.handle(new GetLeaderboardQuery());
        var resources = java.util.stream.IntStream.range(0, players.size())
                .mapToObj(index -> GamificationResourceAssembler.toResource(players.get(index), index + 1)).toList();
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/coin-transactions/me")
    @Operation(summary = "Get current user coin transactions")
    public ResponseEntity<?> getMyCoinTransactions() {
        return ResponseEntity
                .ok(gamificationQueryService.handle(new GetCoinTransactionsQuery(currentUserService.username())).stream()
                        .map(GamificationResourceAssembler::toResource).toList());
    }

    private ResponseEntity<?> noContentFromResult(Result<String, ApplicationError> result) {
        return switch (result) {
            case Result.Success<String, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<String, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
