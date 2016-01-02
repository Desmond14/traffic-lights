package pl.edu.agh.actors;

import akka.actor.Props;
import akka.japi.Creator;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.messages.IntersectionSurrounding;

import static pl.edu.agh.model.Street.NORTH_SOUTH;
import static pl.edu.agh.model.TrafficLightColor.GREEN;

public class SimpleTrafficLights  extends AbstractTrafficLights {

    public SimpleTrafficLights(TrafficLightsConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void updateState(IntersectionSurrounding intersectionSurrounding) {}

    @Override
    protected boolean shouldSwitchGreenToYellow(IntersectionSurrounding intersectionSurrounding) {
        int expectedGreenLightDuration;
        if (getLightColorOn(NORTH_SOUTH) == GREEN) {
            expectedGreenLightDuration = configuration.northSouthGreenLightDuration;
        } else {
            expectedGreenLightDuration = configuration.westEastGreenLightDuration;
        }
        return getCurrentLightGreenSince() >= expectedGreenLightDuration;
    }

    public static Props props(final TrafficLightsConfiguration configuration) {
        return Props.create(new Creator<SimpleTrafficLights>() {
            public SimpleTrafficLights create() throws Exception {
                return new SimpleTrafficLights(configuration);
            }
        });
    }

}
