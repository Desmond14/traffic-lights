package pl.edu.agh.model;

public class IterationStats {
    public final int carsInSimulation;
    public final int detectedCollisions;
    public final float averageVelocity;

    public IterationStats(int carsInSimulation, int detectedCollisions, float averageVelocity) {
        this.carsInSimulation = carsInSimulation;
        this.detectedCollisions = detectedCollisions;
        this.averageVelocity = averageVelocity;
    }
}
