package pl.edu.agh.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.SurroundingWorldSnapshot;

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
        if (velocity < configuration.maxVelocity) {
            newVelocity = Math.min(velocity + configuration.acceleration, configuration.maxVelocity);
        } else {
            newVelocity = velocity;
        }

        distanceToIntersection -= (newVelocity + velocity)/2;
        velocity = newVelocity;
        log.info("Distance to intersection: " + distanceToIntersection + " , velocity: " + velocity);
        getSender().tell(new DriverUpdate(distanceToIntersection, newVelocity), getSelf());
    }

    public static Props props(final DriverConfiguration driverConfiguration) {
        return Props.create(new Creator<Driver>() {
            public Driver create() throws Exception {
                return new Driver(driverConfiguration);
            }
        });
    }
}
