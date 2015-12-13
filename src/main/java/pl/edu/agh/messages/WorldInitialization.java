package pl.edu.agh.messages;

import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.configuration.WorldConfiguration;

public class WorldInitialization {
    public final DriverConfiguration baseDriverConfiguration;
    public final TrafficLightsConfiguration trafficLightsConfiguration;
    public final WorldConfiguration worldConfiguration;

    public WorldInitialization(DriverConfiguration baseDriverConfiguration, TrafficLightsConfiguration trafficLightsConfiguration, WorldConfiguration worldConfiguration) {
        this.baseDriverConfiguration = baseDriverConfiguration;
        this.trafficLightsConfiguration = trafficLightsConfiguration;
        this.worldConfiguration = worldConfiguration;
    }
}
