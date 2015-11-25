package pl.edu.agh.messages;

import pl.edu.agh.model.Street;
import pl.edu.agh.model.TrafficLightColor;

import java.util.Map;

public class TrafficLightsUpdate {
    public final Map<Street, TrafficLightColor> streetToLightColor;

    public TrafficLightsUpdate(Map<Street, TrafficLightColor> streetToLightColor) {
        this.streetToLightColor = streetToLightColor;
    }
}
