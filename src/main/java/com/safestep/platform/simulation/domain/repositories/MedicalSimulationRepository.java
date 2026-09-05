package com.safestep.platform.simulation.domain.repositories;

import com.safestep.platform.simulation.domain.model.aggregates.MedicalSimulation;
import java.util.*;

public interface MedicalSimulationRepository {
    List<MedicalSimulation> findAll();

    Optional<MedicalSimulation> findBySlug(String slug);

    MedicalSimulation save(MedicalSimulation simulation);

    boolean existsBySlug(String slug);

    void delete(MedicalSimulation simulation);
}
