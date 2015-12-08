package pl.edu.agh.model;

import akka.actor.ActorRef;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.IntersectionSurrounding;
import pl.edu.agh.messages.TrafficGenerationMessage;
import pl.edu.agh.messages.TrafficLightsUpdate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

    public WorldSnapshot(Map<ActorRef,DriverState> driverToState, Map<ActorRef, DriverConfiguration> driverToConfiguration) {
        this.driverToState = driverToState;
        this.driverToConfiguration = driverToConfiguration;
    }

    public void update(ActorRef driver, DriverUpdate updateMessage) {
        driverToState.put(driver, new DriverState(
                driverToState.get(driver).getStreet(),
                updateMessage.newDistanceToIntersection,
                updateMessage.currentVelocity)
        );
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
        return driverToState.keySet();
    }

    public TrafficLightColor getLightColorOnStreet(Street street) {
        return streetToLightColor.get(street);
    }

    public Set<ActorRef> getDriversOnStreet(Street street) {
        Set<ActorRef> result = new HashSet<ActorRef>();
        for (ActorRef driver : driverToState.keySet()) {
            if (street.equals(driverToState.get(driver).getStreet())) {
                result.add(driver);
            }
        }
        return result;
    }

    public WorldSnapshot copy() {
        return new WorldSnapshot(copy(driverToState), driverToConfiguration);
    }

    private Map<ActorRef, DriverState> copy(Map<ActorRef, DriverState> original) {
        Map<ActorRef, DriverState> result = new HashMap<ActorRef, DriverState>();
        for (ActorRef driver : original.keySet()) {
            DriverState originalState = original.get(driver);
            result.put(driver, new DriverState(
                    originalState.getStreet(),
                    originalState.getPositionOnStreet(),
                    originalState.getCurrentVelocity()
            ));
        }
        return result;
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
}
