package com.safestep.platform.commerce.domain.repositories;

import com.safestep.platform.commerce.domain.model.valueobjects.EmergencyKit;
import java.util.*;

public interface EmergencyKitRepository {
    List<EmergencyKit> findAll();
    void save(EmergencyKit kit);
    boolean existsByExternalId(String id);
}
