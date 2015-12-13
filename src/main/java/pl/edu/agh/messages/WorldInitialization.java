package pl.edu.agh.messages;

import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.model.Street;

import java.util.Map;

public class WorldInitialization {
    public final DriverConfiguration baseDriverConfiguration;
    public final TrafficLightsConfiguration trafficLightsConfiguration;
    public final Map<Street, Float> newCarProbability;

    public WorldInitialization(DriverConfiguration baseDriverConfiguration, TrafficLightsConfiguration trafficLightsConfiguration, Map<Street, Float> newCarProbability) {
        this.baseDriverConfiguration = baseDriverConfiguration;
        this.trafficLightsConfiguration = trafficLightsConfiguration;
        this.newCarProbability = newCarProbability;
    }
}
