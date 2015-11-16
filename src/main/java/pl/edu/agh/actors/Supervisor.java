package pl.edu.agh.actors;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.SurroundingWorldSnapshot;
import pl.edu.agh.messages.WorldInitialization;

public class Supervisor extends UntypedActor {
    private static final Integer HORIZONTAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING = 15;
    private static final Integer VERTICAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING = 15;
    private ActorRef horizontalDriver;
    private Integer horizontalDriverDistanceToCrossing;
    private ActorRef verticalDriver;
    private Integer verticalDriverDistanceToCrossing;
    private int countDown = 2;
    private int iteration = 0;

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof WorldInitialization) {
            init((WorldInitialization) message);
        } else if (message instanceof DriverUpdate) {
            updateWorldState((DriverUpdate) message);
            countDown--;
            if (countDown == 0) {
                detectCollisions();
                if (iteration++ < 20) {
                    broadcastWorldSnapshot();
                    countDown = 2;
                }
            }
        }
    }

    private void detectCollisions() {

    }

    private void init(WorldInitialization message) {
        horizontalDriver = this.getContext().actorOf(Driver.props(getDriverConfiguration(HORIZONTAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING)));
        horizontalDriverDistanceToCrossing = HORIZONTAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING;
        verticalDriver = this.getContext().actorOf(Driver.props(getDriverConfiguration(HORIZONTAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING)));
        verticalDriverDistanceToCrossing = VERTICAL_DRIVER_INITIAL_DISTANCE_TO_CROSSING;
        broadcastWorldSnapshot();
    }

    private DriverConfiguration getDriverConfiguration(Integer initialDistanceToIntersection) {
        return new DriverConfiguration.Builder()
                .acceleration(1)
                .carLength(3)
                .carWidth(2)
                .maxVelocity(3)
                .initialDistanceToIntersection(15)
                .yellowLightGoProbability(0.0f)
                .build();
    }

    private void broadcastWorldSnapshot() {
        horizontalDriver.tell(new SurroundingWorldSnapshot(null, verticalDriverDistanceToCrossing, null), getSelf());
        verticalDriver.tell(new SurroundingWorldSnapshot(null, horizontalDriverDistanceToCrossing, null), getSelf());
    }

    private void updateWorldState(DriverUpdate message) {
        if (getSender().equals(horizontalDriver)) {
            horizontalDriverDistanceToCrossing = message.newDistanceToIntersection;
        } else {
            verticalDriverDistanceToCrossing = message.newDistanceToIntersection;
        }
    }
}
