package pl.edu.agh.messages;

import pl.edu.agh.model.DriverState;

import java.util.Set;

public class StatsUpdate {
    public final int detectedCollisions;
    public final int carsInSimulation;
    public final Set<DriverState> driverStates;

    public StatsUpdate(int detectedCollisions, int carsInSimulation, Set<DriverState> allDriversStates) {
        this.detectedCollisions = detectedCollisions;
        this.carsInSimulation = carsInSimulation;
        this.driverStates = allDriversStates;
    }
}
