package pl.edu.agh.messages;

import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.configuration.WorldConfiguration;
import pl.edu.agh.model.SimulationStats;

import java.util.concurrent.BlockingQueue;

public class WorldInitialization {
    public final DriverConfiguration baseDriverConfiguration;
    public final TrafficLightsConfiguration trafficLightsConfiguration;
    public final WorldConfiguration worldConfiguration;
    public final BlockingQueue<SimulationStats> resultCallback;

    public WorldInitialization(DriverConfiguration baseDriverConfiguration,
                               TrafficLightsConfiguration trafficLightsConfiguration,
                               WorldConfiguration worldConfiguration,
                               BlockingQueue<SimulationStats> resultCallback) {
        this.baseDriverConfiguration = baseDriverConfiguration;
        this.trafficLightsConfiguration = trafficLightsConfiguration;
        this.worldConfiguration = worldConfiguration;
        this.resultCallback = resultCallback;
    }
}
