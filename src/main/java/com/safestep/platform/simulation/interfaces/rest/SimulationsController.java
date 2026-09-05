package com.safestep.platform.simulation.interfaces.rest;

import com.safestep.platform.shared.application.services.CurrentUserService;
import com.safestep.platform.shared.application.result.ApplicationError;
import com.safestep.platform.shared.application.result.Result;
import com.safestep.platform.shared.interfaces.rest.transform.ErrorResponseAssembler;
import com.safestep.platform.shared.interfaces.rest.transform.ResponseEntityAssembler;
import com.safestep.platform.simulation.application.commandservices.SimulationCommandService;
import com.safestep.platform.simulation.application.commandservices.SimulationAttemptCommandService;
import com.safestep.platform.simulation.application.queryservices.SimulationQueryService;
import com.safestep.platform.simulation.domain.model.commands.CreateSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.DeleteSimulationCommand;
import com.safestep.platform.simulation.domain.model.commands.UpdateSimulationCommand;
import com.safestep.platform.simulation.domain.model.queries.GetAllSimulationsQuery;
import com.safestep.platform.simulation.domain.model.queries.GetAttemptsByUsernameQuery;
import com.safestep.platform.simulation.domain.model.queries.GetSimulationBySlugQuery;
import com.safestep.platform.simulation.interfaces.rest.resources.CreateAttemptResource;
import com.safestep.platform.simulation.interfaces.rest.resources.SimulationResource;
import com.safestep.platform.simulation.interfaces.rest.transform.SimulationResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/simulations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Medical Simulations", description = "Medical simulation catalog and attempt endpoints")
public class SimulationsController {

    private final SimulationQueryService simulationQueryService;
    private final SimulationCommandService simulationCommandService;
    private final SimulationAttemptCommandService simulationAttemptCommandService;
    private final CurrentUserService currentUserService;

    public SimulationsController(SimulationQueryService simulationQueryService, SimulationCommandService simulationCommandService,
            SimulationAttemptCommandService simulationAttemptCommandService, CurrentUserService currentUserService) {
        this.simulationQueryService = simulationQueryService;
        this.simulationCommandService = simulationCommandService;
        this.simulationAttemptCommandService = simulationAttemptCommandService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    @Operation(summary = "Get all medical simulations")
    public ResponseEntity<List<SimulationResource>> getAllSimulations() {
        return ResponseEntity.ok(simulationQueryService.handle(new GetAllSimulationsQuery()).stream()
                .map(SimulationResourceAssembler::toResource).toList());
    }

    @GetMapping("/{simulationId}")
    @Operation(summary = "Get medical simulation by identifier")
    public ResponseEntity<?> getSimulationById(@PathVariable String simulationId) {
        return simulationQueryService.handle(new GetSimulationBySlugQuery(simulationId))
                .<ResponseEntity<?>> map(value -> ResponseEntity.ok(SimulationResourceAssembler.toResource(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create medical simulation")
    public ResponseEntity<?> createSimulation(@Valid @RequestBody SimulationResource resource) {
        var result = simulationCommandService
                .handle(new CreateSimulationCommand(SimulationResourceAssembler.toSimulation(resource)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, SimulationResourceAssembler::toResource,
                HttpStatus.CREATED);
    }

    @PutMapping("/{simulationId}")
    @Operation(summary = "Update medical simulation")
    public ResponseEntity<?> updateSimulation(@PathVariable String simulationId,
            @Valid @RequestBody SimulationResource resource) {
        var result = simulationCommandService
                .handle(new UpdateSimulationCommand(simulationId, SimulationResourceAssembler.toSimulation(resource)));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, SimulationResourceAssembler::toResource,
                HttpStatus.OK);
    }

    @DeleteMapping({ "/{simulationId}", "/" })
    @Operation(summary = "Delete medical simulation")
    public ResponseEntity<?> deleteSimulation(@PathVariable(required = false) String simulationId) {
        return noContentFromResult(simulationCommandService.handle(new DeleteSimulationCommand(simulationId == null ? "" : simulationId)));
    }

    @PostMapping("/{simulationId}/attempts")
    @Operation(summary = "Create a medical simulation attempt")
    public ResponseEntity<?> createAttempt(@PathVariable String simulationId,
            @Valid @RequestBody CreateAttemptResource resource) {
        var result = simulationAttemptCommandService
                .handle(SimulationResourceAssembler.toCommand(simulationId, currentUserService.username(), resource));
        return ResponseEntityAssembler.toResponseEntityFromResult(result, SimulationResourceAssembler::toResource,
                org.springframework.http.HttpStatus.CREATED);
    }

    @GetMapping("/attempts/me")
    @Operation(summary = "Get current user simulation attempts")
    public ResponseEntity<?> getMyAttempts() {
        return ResponseEntity
                .ok(simulationQueryService.handle(new GetAttemptsByUsernameQuery(currentUserService.username()))
                        .stream().map(SimulationResourceAssembler::toResource).toList());
    }

    private ResponseEntity<?> noContentFromResult(Result<String, ApplicationError> result) {
        return switch (result) {
            case Result.Success<String, ApplicationError> ignored -> ResponseEntity.noContent().build();
            case Result.Failure<String, ApplicationError> failure ->
                    ErrorResponseAssembler.toErrorResponseFromApplicationError(failure.error());
        };
    }
}
