package pl.edu.agh.messages;

public class DriverUpdate {
    public final Integer newDistanceToIntersection;
    public final Integer currentVelocity;

    public DriverUpdate(Integer newDistanceToIntersection, Integer currentVelocity) {
        this.newDistanceToIntersection = newDistanceToIntersection;
        this.currentVelocity = currentVelocity;
    }
}
