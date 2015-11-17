package pl.edu.agh.model;

import akka.actor.ActorRef;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.DriverUpdate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldSnapshot {
    private Map<ActorRef,DriverState> driverToState = new HashMap<ActorRef, DriverState>();
    private Map<ActorRef, DriverConfiguration> driverToConfiguration = new HashMap<ActorRef, DriverConfiguration>();

    public WorldSnapshot() {};

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

    public void addDriver(ActorRef driver, Street street, DriverConfiguration configuration) {
        driverToConfiguration.put(driver, configuration);
        driverToState.put(driver, new DriverState(
                street,
                configuration.initialDistanceToIntersection,
                0
        ));
    }

    public Set<ActorRef> getAllDrivers() {
        return driverToState.keySet();
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

}
