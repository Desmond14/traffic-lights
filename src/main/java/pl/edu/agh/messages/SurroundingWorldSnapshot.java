package pl.edu.agh.messages;

import pl.edu.agh.model.TrafficLightColor;

public class SurroundingWorldSnapshot {
    public final Integer carAheadDistance;
    public final Integer crossingCarDistanceToIntersection;
    public final TrafficLightColor trafficLightColor;
    public final TrafficLightColor previousTrafficLightColor;

    public SurroundingWorldSnapshot(Integer carAheadDistance, Integer crossingCarDistanceToIntersection, TrafficLightColor trafficLightColor, TrafficLightColor previousTrafficLightColor) {
        this.carAheadDistance = carAheadDistance;
        this.crossingCarDistanceToIntersection = crossingCarDistanceToIntersection;
        this.trafficLightColor = trafficLightColor;
        this.previousTrafficLightColor = previousTrafficLightColor;
    }
}
