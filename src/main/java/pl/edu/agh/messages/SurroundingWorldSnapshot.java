package pl.edu.agh.messages;

import pl.edu.agh.model.TrafficLightColor;

public class SurroundingWorldSnapshot {
    public final Integer carBeforeDistance;
    public final Integer crossingCarDistanceToIntersection;
    public final TrafficLightColor trafficLightColor;

    public SurroundingWorldSnapshot(Integer carBeforeDistance, Integer crossingCarDistanceToIntersection, TrafficLightColor trafficLightColor) {
        this.carBeforeDistance = carBeforeDistance;
        this.crossingCarDistanceToIntersection = crossingCarDistanceToIntersection;
        this.trafficLightColor = trafficLightColor;
    }
}
