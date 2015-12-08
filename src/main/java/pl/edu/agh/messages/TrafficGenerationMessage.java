package pl.edu.agh.messages;

import pl.edu.agh.model.DriverWithConfiguration;
import pl.edu.agh.model.Street;

import java.util.Map;
import java.util.Optional;

public class TrafficGenerationMessage {
    public final Map<Street, Optional<DriverWithConfiguration>> newTraffic;
    public final Boolean isInitial;

    public TrafficGenerationMessage(Map<Street, Optional<DriverWithConfiguration>> newTraffic, Boolean isInitial) {
        this.newTraffic = newTraffic;
        this.isInitial = isInitial;
    }
}
