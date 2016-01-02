package pl.edu.agh.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.messages.IntersectionSurrounding;
import pl.edu.agh.messages.TrafficLightsUpdate;
import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.Street;
import pl.edu.agh.model.TrafficLightColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static pl.edu.agh.model.Street.NORTH_SOUTH;
import static pl.edu.agh.model.Street.WEST_EAST;
import static pl.edu.agh.model.TrafficLightColor.GREEN;
import static pl.edu.agh.model.TrafficLightColor.RED;
import static pl.edu.agh.model.TrafficLightColor.YELLOW;

public class TrafficLights extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final TrafficLightsConfiguration configuration;
    private final Map<Street, TrafficLightColor> streetToLightColor = new HashMap<Street, TrafficLightColor>();
    private final Map<Street, Integer> streetCounters = new HashMap<Street, Integer>();
    private Integer currentLightGreenSince = 0;
    private Integer currentLightYellowSince = 0;

    public TrafficLights(TrafficLightsConfiguration configuration) {
        this.configuration = configuration;
        streetToLightColor.put(WEST_EAST, GREEN);
        streetToLightColor.put(NORTH_SOUTH, RED);
        streetCounters.put(WEST_EAST, 0);
        streetCounters.put(NORTH_SOUTH, 0);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        IntersectionSurrounding intersectionSurrounding = (IntersectionSurrounding) message;
        updateCounters(intersectionSurrounding.streetToDrivers);
        if (isYellowLightOn()) {
            log.info("Light is yellow");
            if (currentLightYellowSince < configuration.yellowLightDuration) {
                currentLightYellowSince++;
            } else {
                switchLightsGreen();
            }
            getSender().tell(new TrafficLightsUpdate(copy(streetToLightColor)), getSelf());
            return;
        }

        currentLightGreenSince++;
        if (isAnyLightGreen() && currentLightGreenSince < configuration.minimumGreenTime) {
            log.info("Minimum green time not reached");
            getSender().tell(new TrafficLightsUpdate(copy(streetToLightColor)), getSelf());
            return;
        }
        if (isAnyLightGreen() && fewCarsLeftInShortDistance(getDriversOnGreen(intersectionSurrounding.streetToDrivers))) {
            log.info("Few cars left in short distance");
            getSender().tell(new TrafficLightsUpdate(copy(streetToLightColor)), getSelf());
            return;
        }

        if (noOneOnGreenDirection(getDriversOnGreen(intersectionSurrounding.streetToDrivers)) && isAnyoneAwaitingOnRed(getDriversOnRed(intersectionSurrounding.streetToDrivers))) {
            log.info("No one waiting on green. Switching lights");
            switchLightsYellow();
        }
        if (redStreetColorCounter() > configuration.counterLimitValue) {
            log.info("Red light counter exceeded. Switching lights");
            switchLightsYellow();
        }
        getSender().tell(new TrafficLightsUpdate(copy(streetToLightColor)), getSelf());
    }

    private boolean isYellowLightOn() {
        return currentLightYellowSince > 0;
    }

    private void updateCounters(Map<Street, Set<DriverState>> streetToDrivers) {
        if (streetToLightColor.get(NORTH_SOUTH).equals(RED)) {
            streetCounters.put(NORTH_SOUTH, streetCounters.get(NORTH_SOUTH) + countAwaiting(streetToDrivers.get(NORTH_SOUTH), configuration.longSupervisedDistance));
        }
        if (streetToLightColor.get(WEST_EAST).equals(RED)) {
            streetCounters.put(WEST_EAST, streetCounters.get(WEST_EAST) + countAwaiting(streetToDrivers.get(WEST_EAST), configuration.longSupervisedDistance));
        }
    }

    private boolean isAnyLightGreen() {
        return streetToLightColor.get(NORTH_SOUTH).equals(GREEN) || streetToLightColor.get(WEST_EAST).equals(GREEN);
    }

    private boolean fewCarsLeftInShortDistance(Set<DriverState> drivers) {
        int awaitingOnGreen = countAwaiting(drivers, configuration.shortSupervisedDistance);
        return awaitingOnGreen < configuration.shortSupervisedDistanceMaxCarsNo && awaitingOnGreen > 0;
    }

    private boolean isAnyoneAwaitingOnRed(Set<DriverState> driversOnRed) {
        return driversOnRed != null && countAwaiting(driversOnRed, configuration.longSupervisedDistance) > 0;
    }

    private boolean noOneOnGreenDirection(Set<DriverState> driversOnGreen) {
        return driversOnGreen == null || countAwaiting(driversOnGreen, configuration.longSupervisedDistance) == 0;
    }

    private void switchLightsYellow() {
        log.info("Switching green light to yellow");
        if (streetToLightColor.get(NORTH_SOUTH).equals(RED)) {
            streetToLightColor.put(WEST_EAST, YELLOW);
        } else {
            streetToLightColor.put(NORTH_SOUTH, YELLOW);
        }
        currentLightYellowSince = 1;
        currentLightGreenSince = 0;
    }

    private void switchLightsGreen() {
        log.info("Switching light to green");
        if (streetToLightColor.get(NORTH_SOUTH).equals(RED)) {
            streetToLightColor.put(NORTH_SOUTH, GREEN);
            streetToLightColor.put(WEST_EAST, RED);
        } else {
            streetToLightColor.put(NORTH_SOUTH, RED);
            streetToLightColor.put(WEST_EAST, GREEN);
        }
        currentLightYellowSince = 0;
    }

    private Integer redStreetColorCounter() {
        if (streetToLightColor.get(NORTH_SOUTH).equals(RED)) {
            return streetCounters.get(NORTH_SOUTH);
        }
        return streetCounters.get(WEST_EAST);
    }

    private Set<DriverState> getDriversOnRed(Map<Street, Set<DriverState>> streetToDrivers) {
        return getDriversOn(streetToDrivers, RED);
    }

    private Set<DriverState> getDriversOnGreen(Map<Street, Set<DriverState>> streetToDrivers) {
        return getDriversOn(streetToDrivers, GREEN);
    }

    private Set<DriverState> getDriversOn(Map<Street, Set<DriverState>> streetToDrivers, TrafficLightColor color) {
        if (streetToLightColor.get(NORTH_SOUTH).equals(color)) {
            return streetToDrivers.get(NORTH_SOUTH);
        }
        return streetToDrivers.get(WEST_EAST);
    }

    private Integer countAwaiting(Set<DriverState> driversBeforeIntersection, Integer distance) {
        int counter = 0;
        for (DriverState driver : driversBeforeIntersection) {
            if (driver.getPositionOnStreet() < distance && driver.getPositionOnStreet() >= 0) {
                counter++;
            }
        }
        return counter;
    }

    private Map<Street, TrafficLightColor> copy(Map<Street, TrafficLightColor> streetToLightColor) {
        return new HashMap<>(streetToLightColor);
    }

    public static Props props(final TrafficLightsConfiguration configuration) {
        return Props.create(new Creator<TrafficLights>() {
            public TrafficLights create() throws Exception {
                return new TrafficLights(configuration);
            }
        });
    }
}
