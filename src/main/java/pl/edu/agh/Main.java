package pl.edu.agh;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import pl.edu.agh.actors.Supervisor;
import pl.edu.agh.messages.WorldInitialization;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("IntersectionSimulation");
        ActorRef supervisor = system.actorOf(Props.create(Supervisor.class), "supervisor");
        supervisor.tell(new WorldInitialization(), null);
        Thread.sleep(5000);
        system.shutdown();
    }
}
