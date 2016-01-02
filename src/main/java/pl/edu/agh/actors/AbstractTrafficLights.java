package pl.edu.agh.actors;

import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import pl.edu.agh.configuration.TrafficLightsConfiguration;
import pl.edu.agh.messages.IntersectionSurrounding;
import pl.edu.agh.messages.TrafficLightsUpdate;
import pl.edu.agh.model.Street;
import pl.edu.agh.model.TrafficLightColor;

import java.util.HashMap;
import java.util.Map;

import static pl.edu.agh.model.Street.NORTH_SOUTH;
import static pl.edu.agh.model.Street.WEST_EAST;
import static pl.edu.agh.model.TrafficLightColor.GREEN;
import static pl.edu.agh.model.TrafficLightColor.RED;
import static pl.edu.agh.model.TrafficLightColor.YELLOW;

public abstract class AbstractTrafficLights extends UntypedActor {
    protected final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    protected final TrafficLightsConfiguration configuration;
    private final Map<Street, TrafficLightColor> streetToLightColor = new HashMap<Street, TrafficLightColor>();
    private Integer currentLightGreenSince = 0;
    private Integer currentLightYellowSince = 0;

    protected AbstractTrafficLights(TrafficLightsConfiguration configuration) {
        this.configuration = configuration;
        streetToLightColor.put(WEST_EAST, GREEN);
        streetToLightColor.put(NORTH_SOUTH, RED);
    }

    @Override
    public void onReceive(Object message) throws Exception {
        IntersectionSurrounding intersectionSurrounding = (IntersectionSurrounding) message;
        updateState(intersectionSurrounding);
        if (isYellowLightOn()) {
            log.info("Light is yellow");
            if (currentLightYellowSince < configuration.yellowLightDuration) {
                currentLightYellowSince++;
            } else {
                switchLightsGreen();
            }
            getSender().tell(new TrafficLightsUpdate(new HashMap<>(streetToLightColor)), getSelf());
            return;
        }

        currentLightGreenSince++;

        if (shouldSwitchGreenToYellow(intersectionSurrounding)) {
            switchLightsYellow();
        }
        getSender().tell(new TrafficLightsUpdate(new HashMap<>(streetToLightColor)), getSelf());
    }

    private boolean isYellowLightOn() {
        return streetToLightColor.get(NORTH_SOUTH) == YELLOW || streetToLightColor.get(WEST_EAST) == YELLOW;
    }

    private void switchLightsGreen() {
        if (streetToLightColor.get(NORTH_SOUTH).equals(RED)) {
            streetToLightColor.put(NORTH_SOUTH, GREEN);
            streetToLightColor.put(WEST_EAST, RED);
            log.info("Switching light to green on north-south");
        } else {
            streetToLightColor.put(NORTH_SOUTH, RED);
            streetToLightColor.put(WEST_EAST, GREEN);
            log.info("Switching light to green on west-east");
        }
        currentLightYellowSince = 0;
    }

    private void switchLightsYellow() {
        log.info("Switching green light to yellow");
        if (streetToLightColor.get(NORTH_SOUTH).equals(RED)) {
            streetToLightColor.put(WEST_EAST, YELLOW);
        } else {
            streetToLightColor.put(NORTH_SOUTH, YELLOW);
        }
        currentLightYellowSince = 1;
        currentLightGreenSince = 0;
    }

    protected TrafficLightColor getLightColorOn(Street street) {
        return streetToLightColor.get(street);
    }

    protected int getCurrentLightGreenSince() {
        return currentLightGreenSince;
    }

    protected abstract void updateState(IntersectionSurrounding intersectionSurrounding);

    protected abstract boolean shouldSwitchGreenToYellow(IntersectionSurrounding intersectionSurrounding);

}

