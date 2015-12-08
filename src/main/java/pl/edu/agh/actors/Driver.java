package pl.edu.agh.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import edu.rit.numeric.Quadratic;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.SurroundingWorldSnapshot;

import static java.lang.Double.isNaN;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static pl.edu.agh.model.TrafficLightColor.GREEN;

public class Driver extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final Quadratic quadratic = new Quadratic();
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
                newVelocity = slowDown();
                log.info("Driver is able to stop before intersection.");
            } else {
                newVelocity = followNagelSchreckenberg(snapshot);
                log.info("Not able to stop before intersection. Will follow Nagel-Schreckenberg");
            }
        } else {
            log.info("Driver in safe distance to intersection. Will follow Nagel-Schreckenberg");
            newVelocity = followNagelSchreckenberg(snapshot);
        }

        distanceToIntersection -= (newVelocity + velocity)/2;
        velocity = newVelocity;
        log.info("Distance to intersection: " + distanceToIntersection + " , velocity: " + velocity);
        getSender().tell(new DriverUpdate(distanceToIntersection, newVelocity), getSelf());
    }

    private boolean isInSafeDistanceToIntersection(SurroundingWorldSnapshot snapshot) {
        if (distanceToIntersection <= 0 ) {
            return true;
        }
        quadratic.solve(-configuration.acceleration / (double)2, -velocity, distanceToIntersection);
        Integer timeToSlowDown = extractSolution(quadratic);
        Integer timeToReachIntersection = (int) (distanceToIntersection / (double) velocity);
        return timeToSlowDown + 2 <= timeToReachIntersection;
    }

    private boolean isAbleToStopBeforeIntersection() {
        if (distanceToIntersection <= 0 ) {
            return false;
        }
        quadratic.solve(-configuration.acceleration / (double)2, -velocity, distanceToIntersection);
        Integer timeToSlowDown = extractSolution(quadratic);
        Integer timeToReachIntersection = (int) (distanceToIntersection / (double) velocity);
        return timeToSlowDown <= timeToReachIntersection;
    }

    private Integer extractSolution(Quadratic quadratic) {
        if (quadratic.nRoots == 2) {
            return (int) ceil(max(quadratic.x1, quadratic.x2));
        } else if (quadratic.nRoots == 1) {
            return (int) ceil((isNaN(quadratic.x1) ? quadratic.x2 : quadratic.x1));
        }
        throw new IllegalArgumentException("Solution for quadratic equation not found!");
    }

    private Integer followNagelSchreckenberg(SurroundingWorldSnapshot snapshot) {
        return tryAccelerate();
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
