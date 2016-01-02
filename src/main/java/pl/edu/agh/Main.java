package pl.edu.agh;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import com.google.common.collect.ImmutableMap;
import pl.edu.agh.actors.Supervisor;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.configuration.WorldConfiguration;
import pl.edu.agh.messages.WorldInitialization;
import pl.edu.agh.model.SimulationStats;
import pl.edu.agh.model.Street;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private static final String BASE_DRIVER_CONFIGURATION_FILENAME = "/drivers.properties";
    private static final String WORLD_CONFIGURATION_FILENAME = "/world.properties";
    private static final String TRAFFIC_LIGHTS_FILENAME = "/lights.properties";

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("IntersectionSimulation");
        ActorRef supervisor = system.actorOf(Props.create(Supervisor.class), "supervisor");
        BlockingQueue<SimulationStats> resultCallback = new LinkedBlockingQueue<>();
        supervisor.tell(
                new WorldInitialization(
                        loadBaseDriverConfiguration(),
                        loadTrafficLightsConfiguration(),
                        loadWorldConfiguration(),
                        resultCallback), null
        );
        try {
            SimulationStats result = resultCallback.take();
        } finally {
            system.shutdown();
        }
    }

    private static DriverConfiguration loadBaseDriverConfiguration() {
        Properties prop = loadPropertiesFile(BASE_DRIVER_CONFIGURATION_FILENAME);
        DriverConfiguration configuration = new DriverConfiguration.Builder()
                .acceleration(loadInt(prop, "acceleration"))
                .carLength(loadInt(prop, "carLength"))
                .carWidth(loadInt(prop, "carWidth"))
                .maxVelocity(loadInt(prop, "maxVelocity"))
                .yellowLightGoProbability(loadFloat(prop, "yellowLightGoProbability"))
                .build();
        return configuration;
    }

    private static WorldConfiguration loadWorldConfiguration() {
        Properties prop = loadPropertiesFile(WORLD_CONFIGURATION_FILENAME);
        WorldConfiguration configuration = new WorldConfiguration.Builder()
                .monitoredDistanceFromCrossing(loadInt(prop, "monitoredDistanceFromCrossing"))
                .newCarGenerationProbability(ImmutableMap.<Street, Float>builder()
                        .put(Street.WEST_EAST, loadFloat(prop, "westEastGenerationProbability"))
                        .put(Street.NORTH_SOUTH, loadFloat(prop, "northSouthGenerationProbability"))
                        .build())
                .streetWidth(loadInt(prop, "streetWidth"))
                .useSimpleLights(loadBoolean(prop, "useSimpleLights"))
                .simulationIterations(loadInt(prop, "simulationIterations"))
                .build();
        return configuration;
    }

    private static TrafficLightsConfiguration loadTrafficLightsConfiguration() {
        Properties prop = loadPropertiesFile(TRAFFIC_LIGHTS_FILENAME);
        TrafficLightsConfiguration lightsConfiguration = new TrafficLightsConfiguration.Builder()
                .counterLimitValue(loadInt(prop, "counterLimitValue"))
                .longSupervisedDistance(loadInt(prop, "longSupervisedDistance"))
                .shortSupervisedDistance(loadInt(prop, "shortSupervisedDistance"))
                .shortSupervisedDistanceMaxCarsNo(loadInt(prop, "shortSupervisedDistanceMaxCarNo"))
                .minimumGreenTime(loadInt(prop, "minimumGreenTime"))
                .yellowLightDuration(loadInt(prop, "yellowLightDuration"))
                .northSouthGreenLightDuration(loadInt(prop, "northSouthGreenLightDuration"))
                .westEastGreenLightDuration(loadInt(prop, "westEastGreenLightDuration"))
                .build();
        return lightsConfiguration;
    }

    private static Properties loadPropertiesFile(String filename) {
        try (InputStream inputStream = Main.class.getResourceAsStream(filename)) {
            Properties prop = new Properties();
            prop.load(inputStream);
            return prop;
        } catch (IOException e) {

        }
        throw new IllegalStateException("Could not load properties file!");
    }

    private static Integer loadInt(Properties prop, String key) {
        return Integer.parseInt(prop.getProperty(key));
    }

    private static Float loadFloat(Properties prop, String key) {
        return Float.parseFloat(prop.getProperty(key));
    }

    private static Boolean loadBoolean(Properties prop, String key) {
        return Boolean.parseBoolean(prop.getProperty(key));
    }
}
