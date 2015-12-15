package pl.edu.agh.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.WorldConfiguration;
import pl.edu.agh.messages.*;
import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.Street;
import pl.edu.agh.model.TrafficLightColor;
import pl.edu.agh.model.WorldSnapshot;

public class Supervisor extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private WorldConfiguration worldConfiguration;
    private ActorRef trafficLightsAgent;
    private ActorRef trafficGeneratorAgent;
    private ActorRef statisticsCollector;
    private WorldSnapshot previousSnapshot;
    private WorldSnapshot currentSnapshot;
    private int countDown = 0;
    private int carsInSimulation = 0;
    private int detectedCollisions = 0;
    private int iteration = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof WorldInitialization) {
            init((WorldInitialization) message);
        } else if (message instanceof DriverUpdate) {
            updateWorldState((DriverUpdate) message);
            countDown--;
            if (countDown == 0) {
                if (detectCollisions()) {
                    detectedCollisions++;
                }
                if (iteration++ < worldConfiguration.simulationIterations) {
                    broadcastWorldSnapshot();
                    countDown = carsInSimulation;
                } else {
                    statisticsCollector.tell(new SimulationEnd(), getSelf());
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
                    log.info("Collision detected between " + horizontalDriver + " and " + verticalDriver);
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
        DriverState currentState = currentSnapshot.getDriverState(driver);
        DriverState previousState = currentSnapshot.getDriverState(driver);
        DriverConfiguration configuration = currentSnapshot.getDriverConfiguration(driver);
        Integer startPosition = previousState.getPositionOnStreet();
        Integer endPosition = currentState.getPositionOnStreet() + configuration.carLength;
        return startPosition >= 0 && endPosition <= worldConfiguration.streetWidth;
    }

    private void init(WorldInitialization message) {
        this.worldConfiguration = message.worldConfiguration;
        previousSnapshot = new WorldSnapshot();
        currentSnapshot = previousSnapshot.copy();
        statisticsCollector = this.getContext().actorOf(StatisticsCollector.props(message.baseDriverConfiguration, message.worldConfiguration));
        trafficLightsAgent = this.getContext().actorOf(TrafficLights.props(message.trafficLightsConfiguration), "trafficLights");
        trafficGeneratorAgent = this.getContext().actorOf(
                TrafficGenerator.props(
                        message.worldConfiguration.newCarGenerationProbability,
                        message.baseDriverConfiguration,
                        message.worldConfiguration.monitoredDistanceFromCrossing),
                "trafficGenerator");
        trafficGeneratorAgent.tell(currentSnapshot.getIntersectionSurrouding(true), getSelf());
    }

    private void broadcastWorldSnapshot() {
        for (ActorRef driver : currentSnapshot.getAllDrivers()) {
            driver.tell(new SurroundingWorldSnapshot(currentSnapshot.getCarAheadDistance(driver), null, getLights(currentSnapshot.getDriverState(driver).getStreet())), getSelf());
        }
        trafficLightsAgent.tell(currentSnapshot.getIntersectionSurrouding(false), getSelf());
        trafficGeneratorAgent.tell(currentSnapshot.getIntersectionSurrouding(false), getSelf());
        statisticsCollector.tell(getStatsUpdate(), getSelf());
        detectedCollisions = 0;
    }

    private StatsUpdate getStatsUpdate() {
        return new StatsUpdate(detectedCollisions, carsInSimulation, currentSnapshot.getAllDriversStates());
    }

    private TrafficLightColor getLights(Street street) {
        return currentSnapshot.getLightColorOnStreet(street);
    }

    private void updateWorldState(DriverUpdate message) {
        currentSnapshot.update(getSender(), message);
    }
}
