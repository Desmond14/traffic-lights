package pl.edu.agh.actors;

import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.WorldConfiguration;
import pl.edu.agh.messages.DriverUpdate;
import pl.edu.agh.messages.SimulationEnd;
import pl.edu.agh.messages.StatsUpdate;
import pl.edu.agh.model.DriverState;
import pl.edu.agh.model.IterationStats;

import java.util.ArrayList;
import java.util.List;

public class StatisticsCollector extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final DriverConfiguration baseConfiguration;
    private final WorldConfiguration worldConfiguration;
    private final List<IterationStats> statsPerIteration;

    public StatisticsCollector(DriverConfiguration baseConfiguration, WorldConfiguration worldConfiguration) {
        this.baseConfiguration = baseConfiguration;
        this.worldConfiguration = worldConfiguration;
        this.statsPerIteration = new ArrayList<>();
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StatsUpdate) {
            saveIterationStatistics((StatsUpdate)message);
        } else if (message instanceof SimulationEnd) {
            saveStatisticsToFile();
        }
    }

    private void saveIterationStatistics(StatsUpdate message) {
        IterationStats stats = calculateIterationStats(message);
        statsPerIteration.add(stats);
    }

    private IterationStats calculateIterationStats(StatsUpdate message) {
        return new IterationStats(message.carsInSimulation, message.detectedCollisions, calculateAverageVelocity(message));
    }

    private float calculateAverageVelocity(StatsUpdate message) {
        int summaryVelocity = 0;
        for (DriverState driverState : message.driverStates) {
            summaryVelocity += driverState.getCurrentVelocity();
        }
        return summaryVelocity / (float) message.carsInSimulation;
    }

    private void saveStatisticsToFile() {
        float averageVelocity = calculateAverageVelocityInWholeSimulation();
        int totalNumberOfCollisions = calculateTotalNumberOfCollisions();
        log.info("Average velocity in whole simulation: " + averageVelocity);
        log.info("Total number of detected collisions: " + totalNumberOfCollisions);
    }

    private int calculateTotalNumberOfCollisions() {
        int totalNumberOfCollisions = 0;
        for (IterationStats stats : statsPerIteration) {
            totalNumberOfCollisions += stats.detectedCollisions;
        }
        return totalNumberOfCollisions;
    }

    private float calculateAverageVelocityInWholeSimulation() {
        float totalVelocity = 0;
        int totalCars = 0;
        for (IterationStats stats : statsPerIteration) {
            totalVelocity += stats.averageVelocity * stats.carsInSimulation;
            totalCars += stats.carsInSimulation;
        }
        return totalVelocity / totalCars;
    }

    public static Props props(final DriverConfiguration baseConfiguration, final WorldConfiguration worldConfiguration) {
        return Props.create(new Creator<StatisticsCollector>() {
            @Override
            public StatisticsCollector create() throws Exception {
                return new StatisticsCollector(baseConfiguration, worldConfiguration);
            }
        });
    }
}
