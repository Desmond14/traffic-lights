package pl.edu.agh.messages;

import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.Street;

import java.util.Map;
import java.util.Set;

public class IntersectionSurrounding {
    public final Map<Street, Set<DriverState>> streetToDrivers;
    public final Boolean isInitialMessage;

    public IntersectionSurrounding(Map<Street, Set<DriverState>> streetToDrivers, Boolean isInitialMessage) {
        this.streetToDrivers = streetToDrivers;
        this.isInitialMessage = isInitialMessage;
    }
}
