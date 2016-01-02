package pl.edu.agh.actors;

import akka.actor.Props;
import akka.japi.Creator;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.messages.IntersectionSurrounding;
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

public class SelfOrganizingTrafficLights extends AbstractTrafficLights {
    private final Map<Street, Integer> streetCounters = new HashMap<Street, Integer>();

    public SelfOrganizingTrafficLights(TrafficLightsConfiguration configuration) {
        super(configuration);
        streetCounters.put(WEST_EAST, 0);
        streetCounters.put(NORTH_SOUTH, 0);
    }

    @Override
    protected void updateState(IntersectionSurrounding intersectionSurrounding) {
        updateCounters(intersectionSurrounding.streetToDrivers);
    }

    @Override
    protected boolean shouldSwitchGreenToYellow(IntersectionSurrounding intersectionSurrounding) {
        if (getCurrentLightGreenSince() < configuration.minimumGreenTime) {
            log.info("Minimum green time not reached");
            return false;
        }
        if (fewCarsLeftInShortDistance(getDriversOnGreen(intersectionSurrounding.streetToDrivers))) {
            log.info("Few cars left in short distance");
            return false;
        }
        if (noOneOnGreenDirection(getDriversOnGreen(intersectionSurrounding.streetToDrivers)) && isAnyoneAwaitingOnRed(getDriversOnRed(intersectionSurrounding.streetToDrivers))) {
            log.info("No one waiting on green. Switching lights");
            return true;
        }
        if (redStreetColorCounter() > configuration.counterLimitValue) {
            log.info("Red light counter exceeded. Switching lights");
            return true;
        }
        return false;
    }

    private void updateCounters(Map<Street, Set<DriverState>> streetToDrivers) {
        if (getLightColorOn(NORTH_SOUTH).equals(RED)) {
            streetCounters.put(NORTH_SOUTH, streetCounters.get(NORTH_SOUTH) + countAwaiting(streetToDrivers.get(NORTH_SOUTH), configuration.longSupervisedDistance));
        }
        if (getLightColorOn(WEST_EAST).equals(RED)) {
            streetCounters.put(WEST_EAST, streetCounters.get(WEST_EAST) + countAwaiting(streetToDrivers.get(WEST_EAST), configuration.longSupervisedDistance));
        }
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

    private Integer redStreetColorCounter() {
        if (getLightColorOn(NORTH_SOUTH).equals(RED)) {
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
        if (getLightColorOn(NORTH_SOUTH).equals(color)) {
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
        return Props.create(new Creator<SelfOrganizingTrafficLights>() {
            public SelfOrganizingTrafficLights create() throws Exception {
                return new SelfOrganizingTrafficLights(configuration);
            }
        });
    }
}
