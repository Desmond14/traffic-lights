package pl.edu.agh.messages;

import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.Street;

import java.util.Map;
import java.util.Set;

public class IntersectionSurrounding {
    public final Map<Street, Set<DriverState>> streetToDrivers;

    public IntersectionSurrounding(Map<Street, Set<DriverState>> streetToDrivers) {
        this.streetToDrivers = streetToDrivers;
    }
}
