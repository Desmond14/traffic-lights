package pl.edu.agh.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Creator;
import com.google.common.collect.ImmutableMap;
import pl.edu.agh.configuration.DriverConfiguration;
import pl.edu.agh.configuration.WorldConfiguration;
import pl.edu.agh.messages.SimulationEnd;
import pl.edu.agh.messages.StatsUpdate;
import pl.edu.agh.model.*;

import java.util.*;
import java.util.concurrent.BlockingQueue;

import static pl.edu.agh.model.Street.NORTH_SOUTH;
import static pl.edu.agh.model.Street.WEST_EAST;
import static pl.edu.agh.model.TrafficLightColor.GREEN;
import static pl.edu.agh.model.TrafficLightColor.YELLOW;

public class StatisticsCollector extends UntypedActor {
    private final LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private final DriverConfiguration baseConfiguration;
    private final WorldConfiguration worldConfiguration;
    private final BlockingQueue<SimulationStats> resultCallback;
    private final List<IterationStats> statsPerIteration;
    private final Map<Street, List<Integer>> greenLightDurations;
    private int currentLightGreenSince = 0;

    public StatisticsCollector(DriverConfiguration baseConfiguration,
                               WorldConfiguration worldConfiguration,
                               BlockingQueue<SimulationStats> resultCallback) {
        this.baseConfiguration = baseConfiguration;
        this.worldConfiguration = worldConfiguration;
        this.resultCallback = resultCallback;
        this.statsPerIteration = new LinkedList<>();
        this.greenLightDurations = new HashMap<>();
        greenLightDurations.put(NORTH_SOUTH, new LinkedList<>());
        greenLightDurations.put(WEST_EAST, new LinkedList<>());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof StatsUpdate) {
            saveIterationStatistics((StatsUpdate) message);
            if (justChangedToYellow((StatsUpdate) message)) {
                saveGreenLightDuration((StatsUpdate) message);
            } else if (justChangedToGreen((StatsUpdate) message)) {
                currentLightGreenSince = 0;
            } else if (hasGreenLightOn(((StatsUpdate) message).currentSnapshot)) {
                currentLightGreenSince++;
            }
        } else if (message instanceof SimulationEnd) {
            SimulationStats simulationStats = calculateSimulationStats();
            resultCallback.offer(simulationStats);
        }
    }

    private void saveGreenLightDuration(StatsUpdate message) {
        if (message.previousSnapshot.getLightColorOnStreet(NORTH_SOUTH) == GREEN) {
            greenLightDurations.get(NORTH_SOUTH).add(currentLightGreenSince);
        } else {
            greenLightDurations.get(WEST_EAST).add(currentLightGreenSince);
        }
    }

    private boolean justChangedToYellow(StatsUpdate message) {
        return hasGreenLightOn(message.previousSnapshot) && hasYellowLightOn(message.currentSnapshot);
    }

    private boolean justChangedToGreen(StatsUpdate message) {
        return hasYellowLightOn(message.previousSnapshot) && hasGreenLightOn(message.currentSnapshot);
    }

    private boolean hasGreenLightOn(WorldSnapshot worldSnapshot) {
        return hasLightOn(worldSnapshot, GREEN);
    }

    private boolean hasYellowLightOn(WorldSnapshot snapshot) {
        return hasLightOn(snapshot, YELLOW);
    }

    private boolean hasLightOn(WorldSnapshot worldSnapshot, TrafficLightColor color) {
        return worldSnapshot.getLightColorOnStreet(NORTH_SOUTH) == color || worldSnapshot.getLightColorOnStreet(WEST_EAST) == color;
    }

    private void saveIterationStatistics(StatsUpdate message) {
        IterationStats stats = calculateIterationStats(message);
        statsPerIteration.add(stats);
    }

    private IterationStats calculateIterationStats(StatsUpdate message) {
        Map<Street, Integer> numberOfCarsPerStreet = calculateNumberOfCarsPerStreet(message);
        Map<Street, Integer> numberOfCarsBeforeIntersection = calculateNumberOfCarsBeforeInterection(message);
        int numberOfDriversThatCrossedIntersection = calculateNumberOfDriversThatCrossedIntersection(message);
        int numberOfDriversWaitingOnRedOrYellow = calculateNumberOfDriversWaitingOnRedOrYellow(message);
        Map<Street, Float> averageVelocityPerStreet = calculateAverageVelocity(message);
        Map<Street, Float> averageVelocityBeforeIntersection = calculateAverageVelocityBeforeIntersection(message);
        return new IterationStats.Builder()
                .numberOfCarsPerStreet(numberOfCarsPerStreet)
                .numberOfCarsBeforeIntersection(numberOfCarsBeforeIntersection)
                .numberOfDetectedCollisions(message.detectedCollisions)
                .numberOfDriversThatCrossedIntersection(numberOfDriversThatCrossedIntersection)
                .numberOfDriversWaitingOnRedOrYellow(numberOfDriversWaitingOnRedOrYellow)
                .averageVelocityPerStreet(averageVelocityPerStreet)
                .averageVelocityBeforeIntersection(averageVelocityBeforeIntersection)
                .build();
    }

    private Map<Street, Integer> calculateNumberOfCarsPerStreet(StatsUpdate message) {
        return ImmutableMap.<Street, Integer>builder()
                .put(NORTH_SOUTH, message.currentSnapshot.getDriversOnStreet(NORTH_SOUTH).size())
                .put(WEST_EAST, message.currentSnapshot.getDriversOnStreet(WEST_EAST).size())
                .build();
    }

    private Map<Street, Integer> calculateNumberOfCarsBeforeInterection(StatsUpdate message) {
        return ImmutableMap.<Street, Integer>builder()
                .put(NORTH_SOUTH, message.currentSnapshot.getDriversBeforeIntersectionOnStreet(NORTH_SOUTH).size())
                .put(WEST_EAST, message.currentSnapshot.getDriversBeforeIntersectionOnStreet(WEST_EAST).size())
                .build();
    }

    private int calculateNumberOfDriversThatCrossedIntersection(StatsUpdate message) {
        Set<ActorRef> driversBeforeIntersection = message.previousSnapshot.getDriversBeforeIntersectionOnStreet(NORTH_SOUTH);
        driversBeforeIntersection.addAll(message.previousSnapshot.getDriversBeforeIntersectionOnStreet(WEST_EAST));

        Set<ActorRef> driversAfterIntersection = message.currentSnapshot.getAllDrivers();
        driversAfterIntersection.removeAll(message.currentSnapshot.getDriversBeforeIntersectionOnStreet(NORTH_SOUTH));
        driversAfterIntersection.removeAll(message.currentSnapshot.getDriversBeforeIntersectionOnStreet(WEST_EAST));

        int count = 0;
        for (ActorRef driverBeforeIntersection : driversBeforeIntersection) {
            if (driversAfterIntersection.contains(driverBeforeIntersection)) {
                count++;
            }
        }
        return count;
    }

    private int calculateNumberOfDriversWaitingOnRedOrYellow(StatsUpdate message) {
        return getDriversAwaitingBeforeRedOrYellow(message, NORTH_SOUTH) + getDriversAwaitingBeforeRedOrYellow(message, WEST_EAST);
    }

    private int getDriversAwaitingBeforeRedOrYellow(StatsUpdate message, Street street) {
        if (message.currentSnapshot.getLightColorOnStreet(street) != GREEN) {
            return message.currentSnapshot.getDriversBeforeIntersectionOnStreet(street).size();
        }
        return 0;
    }

    private Map<Street, Float> calculateAverageVelocity(StatsUpdate message) {
        return ImmutableMap.<Street, Float>builder()
                .put(NORTH_SOUTH, calculateAverageVelocityOnStreet(message, NORTH_SOUTH))
                .put(WEST_EAST, calculateAverageVelocityOnStreet(message, WEST_EAST))
                .build();
    }

    private Float calculateAverageVelocityOnStreet(StatsUpdate message, Street street) {
        Set<ActorRef> drivers = message.currentSnapshot.getDriversOnStreet(street);
        return calculateAverageVelocity(message, drivers);
    }

    private Map<Street, Float> calculateAverageVelocityBeforeIntersection(StatsUpdate message) {
        return ImmutableMap.<Street, Float>builder()
                .put(NORTH_SOUTH, calculateAverageVelocityBeforeIntersectionOnStreet(message, NORTH_SOUTH))
                .put(WEST_EAST, calculateAverageVelocityBeforeIntersectionOnStreet(message, WEST_EAST))
                .build();
    }

    private Float calculateAverageVelocityBeforeIntersectionOnStreet(StatsUpdate message, Street street) {
        Set<ActorRef> drivers = message.currentSnapshot.getDriversBeforeIntersectionOnStreet(street);
        return calculateAverageVelocity(message, drivers);
    }

    private Float calculateAverageVelocity(StatsUpdate message, Set<ActorRef> drivers) {
        float totalVelocity = 0.0f;
        for (ActorRef driver : drivers) {
            totalVelocity += message.currentSnapshot.getDriverState(driver).getCurrentVelocity();
        }
        return drivers.size() == 0? 0.0f : (totalVelocity / drivers.size());
    }

    private SimulationStats calculateSimulationStats() {
        float averageVelocity = calculateAverageVelocityInWholeSimulation();
        int totalNumberOfCollisions = calculateTotalNumberOfCollisions();
        float averageNumberOfIntersectionCrossings = calculateAverageNumberOfIntersectionCrossing();
        float averageNumberOfCarsWaitingOnRedOrYellow = calculateAverageNumberOfCarsWaitingOnRedOrYellow();
        float averageGreenLightDurationOnNorthSouth = calculateAverageGreenLightDuration(NORTH_SOUTH);
        float averageGreenLightDurationOnWestEast = calculateAverageGreenLightDuration(WEST_EAST);
        return new SimulationStats(averageVelocity, totalNumberOfCollisions,
                averageNumberOfIntersectionCrossings, averageNumberOfCarsWaitingOnRedOrYellow,
                averageGreenLightDurationOnNorthSouth, averageGreenLightDurationOnWestEast);
    }

    private float calculateAverageGreenLightDuration(Street street) {
        float totalGreenLightDuration = 0.0f;
        for (Integer greenLightDuration : greenLightDurations.get(street)) {
            totalGreenLightDuration += greenLightDuration;
        }
        return totalGreenLightDuration / greenLightDurations.get(street).size();
    }

    private int calculateTotalNumberOfCollisions() {
        int totalNumberOfCollisions = 0;
        for (IterationStats stats : statsPerIteration) {
            totalNumberOfCollisions += stats.numberOfDetectedCollisions;
        }
        return totalNumberOfCollisions;
    }

    private float calculateAverageVelocityInWholeSimulation() {
        float totalVelocity = 0.0f;
        int totalCars = 0;
        for (IterationStats stats : statsPerIteration) {
            totalVelocity += stats.averageVelocityPerStreet.get(NORTH_SOUTH) * stats.numberOfCarsPerStreet.get(NORTH_SOUTH);
            totalVelocity += stats.averageVelocityPerStreet.get(WEST_EAST) * stats.numberOfCarsPerStreet.get(WEST_EAST);
            totalCars += stats.numberOfCarsPerStreet.get(NORTH_SOUTH);
            totalCars += stats.numberOfCarsPerStreet.get(WEST_EAST);
        }
        return totalVelocity / totalCars;
    }

    private float calculateAverageNumberOfIntersectionCrossing() {
        float totalCrossings = 0.0f;
        for (IterationStats stats : statsPerIteration) {
            totalCrossings += stats.numberOfDriversThatCrossedIntersection;
        }
        return totalCrossings / statsPerIteration.size();
    }

    private float calculateAverageNumberOfCarsWaitingOnRedOrYellow() {
        float totalNumberOfWaitingCars = 0.0f;
        for (IterationStats stats : statsPerIteration) {
            totalNumberOfWaitingCars += stats.numberOfDriversWaitingOnRedOrYellow;
        }
        return totalNumberOfWaitingCars / statsPerIteration.size();
    }

    public static Props props(final DriverConfiguration baseConfiguration,
                              final WorldConfiguration worldConfiguration,
                              final BlockingQueue<SimulationStats> resultCallback) {
        return Props.create(new Creator<StatisticsCollector>() {
            @Override
            public StatisticsCollector create() throws Exception {
                return new StatisticsCollector(baseConfiguration, worldConfiguration, resultCallback);
            }
        });
    }
}
