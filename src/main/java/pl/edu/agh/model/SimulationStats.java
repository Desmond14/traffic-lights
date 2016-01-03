package pl.edu.agh.model;

public final class SimulationStats {
    public final float averageVelocity;
    public final int totalNumberOfCollisions;
    public final float averageNumberOfIntersectionCrossings;
    public final float averageNumberOfCarsWaitingOnRedOrYellow;
    public final float averageGreenLightDurationOnNorthSouth;
    public final float averageGreenLightDurationOnWestEast;

    public SimulationStats(float averageVelocity,
                           int totalNumberOfCollisions,
                           float averageNumberOfIntersectionCrossings,
                           float averageNumberOfCarsWaitingOnRedOrYellow,
                           float averageGreenLightDurationOnNorthSouth,
                           float averageGreenLightDurationOnWestEast) {
        this.averageVelocity = averageVelocity;
        this.totalNumberOfCollisions = totalNumberOfCollisions;
        this.averageNumberOfIntersectionCrossings = averageNumberOfIntersectionCrossings;
        this.averageNumberOfCarsWaitingOnRedOrYellow = averageNumberOfCarsWaitingOnRedOrYellow;
        this.averageGreenLightDurationOnNorthSouth = averageGreenLightDurationOnNorthSouth;
        this.averageGreenLightDurationOnWestEast = averageGreenLightDurationOnWestEast;
    }
}
