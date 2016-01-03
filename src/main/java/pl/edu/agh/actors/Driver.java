package pl.edu.agh.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.SurroundingWorldSnapshot;

import static pl.edu.agh.model.TrafficLightColor.GREEN;
import static pl.edu.agh.model.TrafficLightColor.YELLOW;

public class Driver extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final DriverConfiguration configuration;
    private Integer velocity = 0;
    private Integer distanceToIntersection;
    private boolean decidedForYellowGo = false;
    private boolean decidedToSlowDown = false;

    public Driver(DriverConfiguration configuration) {
        this.configuration = configuration;
        this.distanceToIntersection = configuration.initialDistanceToIntersection;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        SurroundingWorldSnapshot snapshot = (SurroundingWorldSnapshot) message;
        Integer newVelocity;
        if (distanceToIntersection < 1) {
            log.info("Driver is on or behind crossing. Will follow NaSch");
            newVelocity = followNagelSchreckenberg(snapshot);
        } else if (snapshot.trafficLightColor == GREEN) {
            decidedToSlowDown = false;
            log.info("Green light on. Will follow NaSch");
            newVelocity = followNagelSchreckenberg(snapshot);
        } else if (decidedToSlowDown) {
            if (velocity == 0) {
                log.info("Staying at 0 velocity");
                newVelocity = velocity;
            } else {
                log.info("Slowing down because of earlier decision due to lights");
                newVelocity = slowDown();
            }
        } else if (isInSafeDistanceToIntersection(snapshot)) {
            log.info("Is in safe distance to intersection");
            newVelocity = followNagelSchreckenberg(snapshot);
        } else if (meetsCriteriaForYellowGo(snapshot)) {
            decidedForYellowGo = true;
            log.info("Following NaSch because of yellow go");
            newVelocity = followNagelSchreckenberg(snapshot);
        } else if (isAbleToStopBeforeIntersection()) {
            decidedToSlowDown = true;
            log.info("Slowing down because traffic light is " + snapshot.trafficLightColor);
            newVelocity = slowDown();
        } else {
            log.info("Not able to stop before intersection. Will follow NaSch");
            newVelocity = followNagelSchreckenberg(snapshot);
        }
        velocity = newVelocity;
        distanceToIntersection -= newVelocity;
        log.info("Distance to intersection: " + distanceToIntersection + " , velocity: " + velocity);
        getSender().tell(new DriverUpdate(distanceToIntersection, newVelocity), getSelf());
    }

    private boolean meetsCriteriaForYellowGo(SurroundingWorldSnapshot snapshot) {
        if (decidedForYellowGo == true || (lightsJustChangedToYellow(snapshot) && configuration.yellowLightGoProbability > Math.random())) {
            return true;
        }
        return false;
    }

    private boolean lightsJustChangedToYellow(SurroundingWorldSnapshot snapshot) {
        return snapshot.previousTrafficLightColor != YELLOW && snapshot.trafficLightColor == YELLOW;
    }

    private boolean isInSafeDistanceToIntersection(SurroundingWorldSnapshot snapshot) {
        Integer timeToSlowDown = calculateTimeToStop();
        Integer timeToReachIntersection = (int) (distanceToIntersection / (double) velocity);
        return timeToSlowDown < timeToReachIntersection;
    }

    private boolean isAbleToStopBeforeIntersection() {
        if (distanceToIntersection <= 0) {
            return false;
        }
        int distanceToStop = calculateDistanceToStop();
        return distanceToStop < distanceToIntersection;
    }

    private Integer calculateTimeToStop() {
        int timeToStop = 0;
        int velocityInStep = this.velocity;
        while (velocityInStep > 0) {
            velocityInStep -= 1;
            timeToStop++;
        }
        return timeToStop;
    }

    private int calculateDistanceToStop() {
        int distanceToStop = 0;
        int velocityInStep = this.velocity;
        do {
            velocityInStep -= configuration.acceleration;
            distanceToStop += velocityInStep;
        } while (velocityInStep > 0);
        return distanceToStop;
    }

    private Integer followNagelSchreckenberg(SurroundingWorldSnapshot snapshot) {
        Integer newVelocity = tryAccelerate();
        newVelocity = Math.min(snapshot.carAheadDistance, newVelocity);
        if (Math.random() < 0.1) {
            newVelocity = Math.max(0, newVelocity - 1);
        }
        return newVelocity;
    }

    private Integer slowDown() {
        int minimalRequiredAcceleration = configuration.acceleration;
        while (minimalRequiredAcceleration > 0 && isAbleToStopWithAcceleration(minimalRequiredAcceleration)) {
            minimalRequiredAcceleration--;
        }
        log.info("Slowing down by " + minimalRequiredAcceleration+1);
        return Math.max(0, velocity - minimalRequiredAcceleration-1);
    }

    private boolean isAbleToStopWithAcceleration(int minimalRequiredAcceleration) {
        int currentVelocity = velocity;
        int distanceLeft = distanceToIntersection;
        while (currentVelocity > 0) {
            currentVelocity -= minimalRequiredAcceleration;
            distanceLeft -= currentVelocity;
        }
        return distanceLeft > 0;
    }

    private Integer tryAccelerate() {
        Integer newVelocity;
        if (velocity < configuration.maxVelocity) {
            newVelocity = Math.min(velocity + configuration.acceleration, configuration.maxVelocity);
        } else {
            newVelocity = velocity;
        }
        return newVelocity;
    }

    public static Props props(final DriverConfiguration driverConfiguration) {
        return Props.create(new Creator<Driver>() {
            @Override
            public Driver create() throws Exception {
                return new Driver(driverConfiguration);
            }
        });
    }

}
