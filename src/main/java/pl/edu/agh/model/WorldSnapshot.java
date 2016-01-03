package pl.edu.agh.model;

import akka.actor.ActorRef;
import com.google.common.collect.ImmutableMap;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.IntersectionSurrounding;
import pl.edu.agh.messages.TrafficGenerationMessage;
import pl.edu.agh.messages.TrafficLightsUpdate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.edu.agh.model.Street.NORTH_SOUTH;
import static pl.edu.agh.model.Street.WEST_EAST;
import static pl.edu.agh.model.TrafficLightColor.GREEN;
import static pl.edu.agh.model.TrafficLightColor.RED;

public class WorldSnapshot {
    public static final int INITIAL_VELOCITY = 0;
    private Map<ActorRef, DriverState> driverToState = new HashMap<ActorRef, DriverState>();
    private Map<ActorRef, DriverConfiguration> driverToConfiguration = new HashMap<ActorRef, DriverConfiguration>();
    private Map<Street, TrafficLightColor> streetToLightColor = new HashMap<Street, TrafficLightColor>();

    public WorldSnapshot() {
        streetToLightColor.put(WEST_EAST, GREEN);
        streetToLightColor.put(NORTH_SOUTH, RED);
    }

    public WorldSnapshot(Map<ActorRef,DriverState> driverToState,
                         Map<ActorRef, DriverConfiguration> driverToConfiguration,
                         Map<Street, TrafficLightColor> streetToLightColor) {
        this.driverToState = driverToState;
        this.driverToConfiguration = driverToConfiguration;
        this.streetToLightColor = streetToLightColor;
    }

    public void update(ActorRef driver, DriverUpdate updateMessage) {
        driverToState.put(driver, new DriverState(
                        driverToState.get(driver).getStreet(),
                        updateMessage.newDistanceToIntersection,
                        updateMessage.currentVelocity)
        );
    }

    public void remove(ActorRef driver) {
        driverToConfiguration.remove(driver);
        driverToState.remove(driver);
    }

    public void update(TrafficLightsUpdate update) {
        this.streetToLightColor = update.streetToLightColor;
    }

    public void update(TrafficGenerationMessage message) {
        for (Street street : message.newTraffic.keySet()) {
            message.newTraffic.get(street).ifPresent(
                    driverWithConfig ->  addDriver(driverWithConfig.driver, street, driverWithConfig.configuration)
            );
        }
    }

    public void addDriver(ActorRef driver, Street street, DriverConfiguration configuration) {
        driverToConfiguration.put(driver, configuration);
        driverToState.put(driver, new DriverState(
                street,
                configuration.initialDistanceToIntersection,
                INITIAL_VELOCITY
        ));
    }

    public Set<ActorRef> getAllDrivers() {
        return new HashSet<>(driverToState.keySet());
    }

    public TrafficLightColor getLightColorOnStreet(Street street) {
        return streetToLightColor.get(street);
    }

    public Set<ActorRef> getDriversOnStreet(Street street) {
        Set<ActorRef> result = driverToState.keySet().stream().filter(driver -> street.equals(driverToState.get(driver).getStreet())).collect(Collectors.toSet());
        return result;
    }

    public Set<ActorRef> getDriversBeforeIntersectionOnStreet(Street street) {
        Set<ActorRef> result = new HashSet<>();
        for (ActorRef driver : getDriversOnStreet(street)) {
            DriverState state = driverToState.get(driver);
            if (state.getPositionOnStreet() > 0) {
                result.add(driver);
            }
        }
        return result;
    }

    public WorldSnapshot copy() {
        return new WorldSnapshot(
                new HashMap<>(driverToState),
                new HashMap<>(driverToConfiguration),
                new HashMap<>(streetToLightColor)
        );
    }

    public DriverState getDriverState(ActorRef driver) {
        return driverToState.get(driver);
    }

    public DriverConfiguration getDriverConfiguration(ActorRef driver) {
        return driverToConfiguration.get(driver);
    }

    public IntersectionSurrounding getIntersectionSurrouding(Boolean isInitialMessage) {
        final Set<DriverState> northSouthDrivers = new HashSet<DriverState>();
        final Set<DriverState> eastWestDrivers = new HashSet<DriverState>();
        for (ActorRef driver : driverToState.keySet()) {
            DriverState state = driverToState.get(driver);
            if (state.getPositionOnStreet() < 0) {
                continue;
            }
            if (state.getStreet() == WEST_EAST) {
                eastWestDrivers.add(state);
            } else {
                northSouthDrivers.add(state);
            }
        }
        return  new IntersectionSurrounding(
                new HashMap<Street, Set<DriverState>>() {{
                    put(WEST_EAST, eastWestDrivers);
                    put(NORTH_SOUTH, northSouthDrivers);
                }}, isInitialMessage);
    }

    public Integer getCarAheadDistance(ActorRef driver) {
        Set<ActorRef> driversOnSameStreet = getDriversOnStreet(driverToState.get(driver).getStreet());
        driversOnSameStreet.remove(driver);
        Integer nearestDistanceAhead = Integer.MAX_VALUE;
        for (ActorRef driverOnSameStreet : driversOnSameStreet) {
            if (isAhead(driver, driverOnSameStreet)) {
                if (distance(driver, driverOnSameStreet) < nearestDistanceAhead) {
                    nearestDistanceAhead = distance(driver, driverOnSameStreet);
                }
            }
        }
        return nearestDistanceAhead;
    }

    public Set<DriverState> getAllDriversStates() {
        return new HashSet<>(driverToState.values());
    }

    private boolean isAhead(ActorRef driver, ActorRef driverOnSameStreet) {
        return driverToState.get(driver).getPositionOnStreet() > driverToState.get(driverOnSameStreet).getPositionOnStreet();
    }

    private Integer distance(ActorRef driver, ActorRef driverOnSameStreet) {
        return driverToState.get(driver).getPositionOnStreet() - driverToState.get(driverOnSameStreet).getPositionOnStreet();
    }
}
