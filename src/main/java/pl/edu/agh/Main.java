package pl.edu.agh;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import pl.edu.agh.actors.Driver;
import pl.edu.agh.actors.Supervisor;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.WorldInitialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Main {
    private static final String BASE_DRIVER_CONFIGURATION_FILENAME = "/drivers.properties";

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("IntersectionSimulation");
        DriverConfiguration baseDriverConfiguration = loadBaseDriverConfiguration();
        ActorRef supervisor = system.actorOf(Props.create(Supervisor.class), "supervisor");
        supervisor.tell(new WorldInitialization(baseDriverConfiguration, null, null), null);
        Thread.sleep(5000);
        system.shutdown();
    }

    private static DriverConfiguration loadBaseDriverConfiguration() {
        Properties prop = loadPropertiesFile(BASE_DRIVER_CONFIGURATION_FILENAME);
        DriverConfiguration configuration = new DriverConfiguration.Builder()
                .acceleration(Integer.parseInt(prop.getProperty("acceleration")))
                .carLength(Integer.parseInt(prop.getProperty("carLength")))
                .carWidth(Integer.parseInt(prop.getProperty("carWidth")))
                .maxVelocity(Integer.parseInt(prop.getProperty("maxVelocity")))
                .yellowLightGoProbability(Float.parseFloat(prop.getProperty("yellowLightGoProbability")))
                .build();
        return configuration;
    }

    private static Properties loadPropertiesFile(String filename) {
        try(InputStream inputStream = Main.class.getResourceAsStream(filename)) {
            Properties prop = new Properties();
            prop.load(inputStream);
            return prop;
        } catch(IOException e) {

        }
        throw new IllegalStateException("Could not load properties file!");
    }
}
