package pl.edu.agh.messages;

import pl.edu.agh.model.TrafficLightColor;

public class SurroundingWorldSnapshot {
    public final Integer carAheadDistance;
    public final Integer crossingCarDistanceToIntersection;
    public final TrafficLightColor trafficLightColor;

    public SurroundingWorldSnapshot(Integer carAheadDistance, Integer crossingCarDistanceToIntersection, TrafficLightColor trafficLightColor) {
        this.carAheadDistance = carAheadDistance;
        this.crossingCarDistanceToIntersection = crossingCarDistanceToIntersection;
        this.trafficLightColor = trafficLightColor;
    }
}
