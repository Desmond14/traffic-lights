package pl.edu.agh.model;

import akka.actor.ActorRef;
import pl.edu.agh.configuration.DriverConfiguration;

public class DriverWithConfiguration {
    public final ActorRef driver;
    public final DriverConfiguration configuration;

    public DriverWithConfiguration(ActorRef driver, DriverConfiguration configuration) {
        this.driver = driver;
        this.configuration = configuration;
    }
}
