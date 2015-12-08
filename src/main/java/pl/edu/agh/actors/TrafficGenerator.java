package pl.edu.agh.actors;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.IntersectionSurrounding;
import pl.edu.agh.messages.TrafficGenerationMessage;
import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.DriverWithConfiguration;
import pl.edu.agh.model.Street;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static pl.edu.agh.model.Street.NORTH_SOUTH;
import static pl.edu.agh.model.Street.WEST_EAST;

public class TrafficGenerator extends UntypedActor {
    private static final Integer INITIAL_DISTANCE_TO_CROSSING = 15;
    private static final Integer DEFAULT_CAR_LENGTH = 2;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Map<Street, Float> newCarProbability;

    public TrafficGenerator(Map<Street, Float> newCarProbability) {
        this.newCarProbability = newCarProbability;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof IntersectionSurrounding) {
            getSender().tell(generateTraffic((IntersectionSurrounding)message), getSelf());
        }
    }

    private TrafficGenerationMessage generateTraffic(IntersectionSurrounding message) {
        Map<Street, Optional<DriverWithConfiguration>> newTraffic = new HashMap<>();

        if (message.isInitialMessage) {
            log.info("Received initial message");
            newTraffic.put(NORTH_SOUTH, generateDriverWithConfiguration());
            newTraffic.put(WEST_EAST, generateDriverWithConfiguration());
            return new TrafficGenerationMessage(newTraffic, true);
        }
        else {
            newTraffic.put(NORTH_SOUTH, generateTraffic(isGenerationPossible(message, NORTH_SOUTH), NORTH_SOUTH));
            newTraffic.put(WEST_EAST, generateTraffic(isGenerationPossible(message, WEST_EAST), WEST_EAST));
            return new TrafficGenerationMessage(newTraffic, false);
        }

    }

    private boolean isGenerationPossible(IntersectionSurrounding intersectionSurrounding, Street street) {
        Set<DriverState> drivers = intersectionSurrounding.streetToDrivers.get(street);
        return !isAnyOnStreetBeginning(drivers);
    }

    private boolean isAnyOnStreetBeginning(Set<DriverState> drivers) {
        for (DriverState driver : drivers) {
            if (isOnStreetBeginning(driver)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOnStreetBeginning(DriverState driver) {
        return 15 >= driver.getPositionOnStreet() + DEFAULT_CAR_LENGTH;
    }

    private Optional<DriverWithConfiguration> generateTraffic(boolean isGenerationPossible, Street street) {
        if (isGenerationPossible && (Math.random() < newCarProbability.get(street))) {
            log.info("Generating car on street " + street);
            return generateDriverWithConfiguration();
        }
        return Optional.empty();
    }

    private Optional<DriverWithConfiguration> generateDriverWithConfiguration() {
        DriverConfiguration driverConfiguration = getDriverConfiguration(INITIAL_DISTANCE_TO_CROSSING);
        ActorRef driver = this.getContext().actorOf(Driver.props(getDriverConfiguration(INITIAL_DISTANCE_TO_CROSSING)));
        return Optional.of(new DriverWithConfiguration(driver, driverConfiguration));
    }

    //TODO: this should be randomized with average parameters passed to TrafficGenerator
    private DriverConfiguration getDriverConfiguration(Integer initialDistanceToIntersection) {
        return new DriverConfiguration.Builder()
                .acceleration(1)
                .carLength(3)
                .carWidth(2)
                .maxVelocity(3)
                .initialDistanceToIntersection(initialDistanceToIntersection)
                .yellowLightGoProbability(0.0f)
                .build();
    }

    public static Props props(final Map<Street, Float> newCarProbability) {
        return Props.create(new Creator<TrafficGenerator>() {
            @Override
            public TrafficGenerator create() throws Exception {
                return new TrafficGenerator(newCarProbability);
            }
        });
    }
}
