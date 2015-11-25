package pl.edu.agh.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.SurroundingWorldSnapshot;
import pl.edu.agh.messages.WorldInitialization;
import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.Street;
import pl.edu.agh.model.WorldSnapshot;

public class Supervisor extends UntypedActor {
    private static final Integer HORIZONTAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING = 15;
    private static final Integer VERTICAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING = 15;
    private static final Integer STREET_WIDTH = 3;
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    ActorRef trafficLightsAgent;
    private WorldSnapshot previousSnapshot;
    private WorldSnapshot currentSnapshot;
    private int countDown = 2;
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
                    log.info("Collision detected!");
                }
                if (iteration++ < 20) {
                    broadcastWorldSnapshot();
                    countDown = 2;
                }
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
        DriverConfiguration horizontalDriverConfiguration = getDriverConfiguration(HORIZONTAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING);
        ActorRef horizontalDriver = this.getContext().actorOf(Driver.props(horizontalDriverConfiguration));
        previousSnapshot.addDriver(horizontalDriver, Street.NORTH_SOUTH, horizontalDriverConfiguration);

        DriverConfiguration verticalDriverConfiguration = getDriverConfiguration(VERTICAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING);
        ActorRef verticalDriver = this.getContext().actorOf(Driver.props(getDriverConfiguration(VERTICAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING)));
        previousSnapshot.addDriver(verticalDriver, Street.WEST_EAST, verticalDriverConfiguration);

        TrafficLightsConfiguration trafficLightsConfiguration = getTrafficLightsConfiguration();
        trafficLightsAgent = this.getContext().actorOf(TrafficLights.props(trafficLightsConfiguration));

        currentSnapshot = previousSnapshot.copy();
        broadcastWorldSnapshot();
    }

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
            driver.tell(new SurroundingWorldSnapshot(null, null, null), getSelf());
        }
    }

    private void updateWorldState(DriverUpdate message) {
        currentSnapshot.update(getSender(), message);
    }
}
