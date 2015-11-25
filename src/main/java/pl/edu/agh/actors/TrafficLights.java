package pl.edu.agh.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
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

public class TrafficLights extends UntypedActor {
    private final TrafficLightsConfiguration configuration;
    private final Map<Street, TrafficLightColor> streetToLightColor = new HashMap<Street, TrafficLightColor>();
    private final Map<Street, Integer> streetCounters = new HashMap<Street, Integer>();
    private Integer currentLightGreenSince = 0;

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
        currentLightGreenSince++;
        if (isAnyLightGreen() && currentLightGreenSince < configuration.minimumGreenTime) {
            getSender().tell(new TrafficLightsUpdate(copy(streetToLightColor)), getSelf());
            return;
        }
        if (isAnyLightGreen() && fewCarsLeftInShortDistance(getDriversOnGreen(intersectionSurrounding.streetToDrivers))) {
            getSender().tell(new TrafficLightsUpdate(copy(streetToLightColor)), getSelf());
            return;
        }

        if (noOneOnGreenDirection(getDriversOnGreen(intersectionSurrounding.streetToDrivers)) && isAnyoneAwaitingOnRed(getDriversOnRed(intersectionSurrounding.streetToDrivers))) {
            switchLights();
        }
        if (redStreetColorCounter() > configuration.counterLimitValue) {
            switchLights();
        }
        getSender().tell(new TrafficLightsUpdate(copy(streetToLightColor)), getSelf());
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
        return countAwaiting(drivers, configuration.shortSupervisedDistance) < configuration.shortSupervisedDistanceMaxCarsNo;
    }

    private boolean isAnyoneAwaitingOnRed(Set<DriverState> driversOnRed) {
        return driversOnRed != null && countAwaiting(driversOnRed, configuration.longSupervisedDistance) > 0;
    }

    private boolean noOneOnGreenDirection(Set<DriverState> driversOnGreen) {
        return driversOnGreen == null || countAwaiting(driversOnGreen, configuration.longSupervisedDistance) == 0;
    }

    private void switchLights() {
        if (streetToLightColor.get(NORTH_SOUTH).equals(RED)) {
            streetToLightColor.put(NORTH_SOUTH, GREEN);
            streetToLightColor.put(WEST_EAST, RED);
        } else {
            streetToLightColor.put(NORTH_SOUTH, RED);
            streetToLightColor.put(WEST_EAST, GREEN);
        }
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
            if (driver.getPositionOnStreet() < distance) {
                counter++;
            }
        }
        return counter;
    }

    private Map<Street, TrafficLightColor> copy(Map<Street, TrafficLightColor> streetToLightColor) {
        return new HashMap<Street, TrafficLightColor>(streetToLightColor);
    }

    public static Props props(final TrafficLightsConfiguration configuration) {
        return Props.create(new Creator<TrafficLights>() {
            public TrafficLights create() throws Exception {
                return new TrafficLights(configuration);
            }
        });
    }
}
