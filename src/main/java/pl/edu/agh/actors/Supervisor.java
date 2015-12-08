package pl.edu.agh.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.messages.*;
import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.Street;
import pl.edu.agh.model.TrafficLightColor;
import pl.edu.agh.model.WorldSnapshot;

import java.util.HashMap;
import java.util.Map;

public class Supervisor extends UntypedActor {
    private static final Integer STREET_WIDTH = 3;
    private static final Map<Street, Float> DEFAULT_PROBABILITY = new HashMap<Street, Float>() {{
        put(Street.WEST_EAST, 0.25f);
        put(Street.NORTH_SOUTH, 0.25f);
    }};
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private ActorRef trafficLightsAgent;
    private ActorRef trafficGeneratorAgent;
    private WorldSnapshot previousSnapshot;
    private WorldSnapshot currentSnapshot;
    private int countDown = 0;
    private int carsInSimulation = 0;
    private int iteration = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        //TODO: handle initial generation of drivers
        if (message instanceof WorldInitialization) {
            init((WorldInitialization) message);
        } else if (message instanceof DriverUpdate) {
            updateWorldState((DriverUpdate) message);
            countDown--;
            if (countDown == 0) {
                if (detectCollisions()) {
                    log.info("Collision detected!");
                }
                if (iteration++ < 50) {
                    broadcastWorldSnapshot();
                    countDown = carsInSimulation;
                }
            }
        } else if (message instanceof TrafficLightsUpdate) {
            currentSnapshot.update((TrafficLightsUpdate) message);
        } else if (message instanceof TrafficGenerationMessage) {
            previousSnapshot.update((TrafficGenerationMessage) message);
            currentSnapshot.update((TrafficGenerationMessage) message);
            if (((TrafficGenerationMessage) message).newTraffic.get(Street.WEST_EAST).isPresent()) {
                carsInSimulation++;
            }
            if (((TrafficGenerationMessage) message).newTraffic.get(Street.NORTH_SOUTH).isPresent()) {
                carsInSimulation++;
            }
            if (((TrafficGenerationMessage) message).isInitial) {
                log.info("Broadcasting initial info");
                countDown = carsInSimulation;
                broadcastWorldSnapshot();
            }
        }
    }

    private boolean detectCollisions() {
        for (ActorRef horizontalDriver : currentSnapshot.getDriversOnStreet(Street.WEST_EAST)) {
            for (ActorRef verticalDriver : currentSnapshot.getDriversOnStreet(Street.NORTH_SOUTH)) {
                if (areBothOnIntersection(horizontalDriver, verticalDriver)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean areBothOnIntersection(ActorRef horizontalDriver, ActorRef verticalDriver) {
        return isOnIntersection(horizontalDriver) && isOnIntersection(verticalDriver);
    }

    private boolean isOnIntersection(ActorRef driver) {
        DriverState state = currentSnapshot.getDriverState(driver);
        DriverConfiguration configuration = currentSnapshot.getDriverConfiguration(driver);
        Integer startPosition = state.getPositionOnStreet();
        Integer endPosition = startPosition + configuration.carLength;
        return startPosition >= 0 && endPosition <= STREET_WIDTH;
    }

    private void init(WorldInitialization message) {
        previousSnapshot = new WorldSnapshot();
        currentSnapshot = previousSnapshot.copy();
        trafficLightsAgent = this.getContext().actorOf(TrafficLights.props(getTrafficLightsConfiguration()), "trafficLights");
        trafficGeneratorAgent = this.getContext().actorOf(TrafficGenerator.props(DEFAULT_PROBABILITY), "trafficGenerator");
        trafficGeneratorAgent.tell(currentSnapshot.getIntersectionSurrouding(true), getSelf());
    }

    private TrafficLightsConfiguration getTrafficLightsConfiguration() {
        return new TrafficLightsConfiguration.Builder()
                .longSupervisedDistance(10)
                .counterLimitValue(20)
                .minimumGreenTime(5)
                .shortSupervisedDistance(4)
                .shortSupervisedDistanceMaxCarsNo(2)
                .build();
    }

    private void broadcastWorldSnapshot() {
        for (ActorRef driver : currentSnapshot.getAllDrivers()) {
            driver.tell(new SurroundingWorldSnapshot(null, null, getLights(currentSnapshot.getDriverState(driver).getStreet())), getSelf());
        }
        trafficLightsAgent.tell(currentSnapshot.getIntersectionSurrouding(false), getSelf());
        trafficGeneratorAgent.tell(currentSnapshot.getIntersectionSurrouding(false), getSelf());
    }

    private TrafficLightColor getLights(Street street) {
        return currentSnapshot.getLightColorOnStreet(street);
    }

    private void updateWorldState(DriverUpdate message) {
        currentSnapshot.update(getSender(), message);
    }
}
