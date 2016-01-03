package pl.edu.agh.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.WorldConfiguration;
import pl.edu.agh.messages.*;
import pl.edu.agh.model.*;

public class Supervisor extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private WorldConfiguration worldConfiguration;
    private ActorRef trafficLightsAgent;
    private ActorRef trafficGeneratorAgent;
    private ActorRef statisticsCollectorAgent;
    private WorldSnapshot previousSnapshot;
    private WorldSnapshot currentSnapshot;
    private IterationStatus iterationStatus = new IterationStatus();

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof WorldInitialization) {
            init((WorldInitialization) message);
        } else if (message instanceof DriverUpdate) {
            updateWorldState((DriverUpdate) message);
            iterationStatus.incrementDriverUpdatesCounter();
        } else if (message instanceof TrafficLightsUpdate) {
            currentSnapshot.update((TrafficLightsUpdate) message);
            iterationStatus.markTrafficLightsUpdateReceived();
        } else if (message instanceof TrafficGenerationMessage) {
            previousSnapshot.update((TrafficGenerationMessage) message);
            currentSnapshot.update((TrafficGenerationMessage) message);
            if (((TrafficGenerationMessage) message).isInitial) {
                log.info("Broadcasting initial info");
                broadcastWorldSnapshot();
                iterationStatus.startNewIteration(currentSnapshot.getAllDrivers().size());
            } else {
                iterationStatus.markTrafficGenerationUpdateReceived();
            }
        }
        if (iterationStatus.areAllUpdatesReceived()) {
            if (detectCollisions()) {
                iterationStatus.incrementDetectedCollisionsCounter();
            }
            if (iterationStatus.getIterationNo() < worldConfiguration.simulationIterations) {
                broadcastWorldSnapshot();
                iterationStatus.startNewIteration(currentSnapshot.getAllDrivers().size());
            } else {
                statisticsCollectorAgent.tell(new SimulationEnd(), getSelf());
            }
            previousSnapshot = currentSnapshot.copy();
        }
    }

    private void init(WorldInitialization message) {
        this.worldConfiguration = message.worldConfiguration;
        previousSnapshot = new WorldSnapshot();
        currentSnapshot = previousSnapshot.copy();
        statisticsCollectorAgent = this.getContext().actorOf(StatisticsCollector.props(message.baseDriverConfiguration, message.worldConfiguration, message.resultCallback));
        if (worldConfiguration.useSimpleLights) {
            trafficLightsAgent = this.getContext().actorOf(SimpleTrafficLights.props(message.trafficLightsConfiguration), "simpleTrafficLights");
        } else {
            trafficLightsAgent = this.getContext().actorOf(SelfOrganizingTrafficLights.props(message.trafficLightsConfiguration), "selfOrganizingTrafficLights");
        }
        trafficGeneratorAgent = this.getContext().actorOf(
                TrafficGenerator.props(
                        message.worldConfiguration.newCarGenerationProbability,
                        message.baseDriverConfiguration,
                        message.worldConfiguration.monitoredDistanceFromCrossing),
                "trafficGenerator");
        trafficGeneratorAgent.tell(currentSnapshot.getIntersectionSurrouding(true), getSelf());
    }

    private void updateWorldState(DriverUpdate message) {
        if (message.newDistanceToIntersection < -worldConfiguration.monitoredDistanceFromCrossing) {
            log.info("Removing actor from simulation " + getSender());
            currentSnapshot.remove(getSender());
            context().stop(getSender());
        } else {
            currentSnapshot.update(getSender(), message);
        }
    }

    private void broadcastWorldSnapshot() {
        for (ActorRef driver : currentSnapshot.getAllDrivers()) {
            driver.tell(new SurroundingWorldSnapshot(currentSnapshot.getCarAheadDistance(driver), null, getLights(currentSnapshot.getDriverState(driver).getStreet()),  getLights(previousSnapshot.getDriverState(driver).getStreet())), getSelf());
        }
        trafficLightsAgent.tell(currentSnapshot.getIntersectionSurrouding(false), getSelf());
        trafficGeneratorAgent.tell(currentSnapshot.getIntersectionSurrouding(false), getSelf());
        statisticsCollectorAgent.tell(getStatsUpdate(), getSelf());
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
        DriverState previousState = previousSnapshot.getDriverState(driver);
        DriverConfiguration configuration = currentSnapshot.getDriverConfiguration(driver);
        return (previousState.getPositionOnStreet()+configuration.carLength-1 > -worldConfiguration.streetWidth)
                && (currentState.getPositionOnStreet() < 1);
    }

    private StatsUpdate getStatsUpdate() {
        return new StatsUpdate(iterationStatus.getDetectedCollisionsCounter(), previousSnapshot.copy(), currentSnapshot.copy());
    }

    private TrafficLightColor getLights(Street street) {
        return currentSnapshot.getLightColorOnStreet(street);
    }
}
