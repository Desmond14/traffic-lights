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

    public Driver(DriverConfiguration configuration) {
        this.configuration = configuration;
        this.distanceToIntersection = configuration.initialDistanceToIntersection;
    }

    @Override
    public void onReceive(Object message) throws Exception {
        SurroundingWorldSnapshot snapshot = (SurroundingWorldSnapshot) message;
        Integer newVelocity;
        if (snapshot.trafficLightColor != GREEN && !isInSafeDistanceToIntersection(snapshot)) {
            if (isAbleToStopBeforeIntersection()) {
                if (snapshot.trafficLightColor == YELLOW && (Math.random() < configuration.yellowLightGoProbability)) {
                    log.info("Trying to go on yellow light even though able to stop");
                    newVelocity = followNagelSchreckenberg(snapshot);
                } else {
                    log.info("Driver is able to stop before intersection and will slow down");
                    newVelocity = slowDown();
                }
            } else {
                newVelocity = followNagelSchreckenberg(snapshot);
                log.info("Not able to stop before intersection. Will follow Nagel-Schreckenberg");
            }
        } else {
            log.info("Driver in safe distance to intersection. Will follow Nagel-Schreckenberg");
            newVelocity = followNagelSchreckenberg(snapshot);
        }

        velocity = newVelocity;
        distanceToIntersection -= newVelocity;
        log.info("Distance to intersection: " + distanceToIntersection + " , velocity: " + velocity);
        getSender().tell(new DriverUpdate(distanceToIntersection, newVelocity), getSelf());
    }

    private boolean isInSafeDistanceToIntersection(SurroundingWorldSnapshot snapshot) {
        if (distanceToIntersection <= 0 ) {
            return true;
        }
        Integer timeToSlowDown = calculateTimeToStop();
        Integer timeToReachIntersection = (int) (distanceToIntersection / (double) velocity);
        return timeToSlowDown + 1 <= timeToReachIntersection;
    }

    private boolean isAbleToStopBeforeIntersection() {
        if (distanceToIntersection <= 0 ) {
            return false;
        }
        int distanceToStop = calculateDistanceToStop();
        return distanceToStop < distanceToIntersection;
    }

    private Integer calculateTimeToStop() {
        int timeToStop = 0;
        int velocityInStep = this.velocity;
        while(velocityInStep > 0) {
            velocityInStep -= configuration.acceleration;
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
            newVelocity = Math.max(0, newVelocity-1);
        }
//        log.info("Old velocity: " + velocity, " newVeloctiy: " + newVelocity);
        return newVelocity;
    }

    private Integer slowDown() {
        return Math.max(0, velocity - configuration.acceleration);
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
