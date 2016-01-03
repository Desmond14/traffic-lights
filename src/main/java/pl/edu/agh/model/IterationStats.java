package pl.edu.agh.model;

import java.util.Map;

public class IterationStats {
    public final Map<Street, Integer> numberOfCarsPerStreet;
    public final Map<Street, Integer> numberOfCarsBeforeIntersection;
    public final int numberOfDetectedCollisions;
    public final int numberOfDriversThatCrossedIntersection;
    public final int numberOfDriversWaitingOnRedOrYellow;
    public final Map<Street, Float> averageVelocityPerStreet;
    public final Map<Street, Float> averageVelocityBeforeIntersection;

    public static class Builder {
        private Map<Street, Integer> numberOfCarsPerStreet;
        private Map<Street, Integer> numberOfCarsBeforeIntersection;
        private int numberOfDetectedCollisions;
        private int numberOfDriversThatCrossedIntersection;
        private int numberOfDriversWaitingOnRedOrYellow;
        private Map<Street, Float> averageVelocityPerStreet;
        private Map<Street, Float> averageVelocityBeforeIntersection;

        public Builder numberOfCarsPerStreet(Map<Street, Integer> numberOfCarsPerStreet) {
            this.numberOfCarsPerStreet = numberOfCarsPerStreet;
            return this;
        }

        public Builder numberOfCarsBeforeIntersection(Map<Street, Integer> numberOfCarsBeforeIntersection) {
            this.numberOfCarsBeforeIntersection = numberOfCarsBeforeIntersection;
            return this;
        }

        public Builder numberOfDetectedCollisions(int numberOfDetectedCollisions) {
            this.numberOfDetectedCollisions = numberOfDetectedCollisions;
            return this;
        }

        public Builder numberOfDriversThatCrossedIntersection(int numberOfDriversThatCrossedIntersection) {
            this.numberOfDriversThatCrossedIntersection = numberOfDriversThatCrossedIntersection;
            return this;
        }

        public Builder numberOfDriversWaitingOnRedOrYellow(int numberOfDriversWaitingOnRedOrYellow) {
            this.numberOfDriversWaitingOnRedOrYellow = numberOfDriversWaitingOnRedOrYellow;
            return this;
        }

        public Builder averageVelocityPerStreet(Map<Street, Float> averageVelocityPerStreet) {
            this.averageVelocityPerStreet = averageVelocityPerStreet;
            return this;
        }

        public Builder averageVelocityBeforeIntersection(Map<Street, Float> averageVelocityBeforeIntersection) {
            this.averageVelocityBeforeIntersection = averageVelocityBeforeIntersection;
            return this;
        }

        public IterationStats build() {
            return new IterationStats(this);
        }
    }

    private IterationStats(Builder builder) {
        this.numberOfCarsPerStreet = builder.numberOfCarsPerStreet;
        this.numberOfCarsBeforeIntersection = builder.numberOfCarsBeforeIntersection;
        this.numberOfDriversThatCrossedIntersection = builder.numberOfDriversThatCrossedIntersection;
        this.numberOfDriversWaitingOnRedOrYellow = builder.numberOfDriversWaitingOnRedOrYellow;
        this.averageVelocityPerStreet = builder.averageVelocityPerStreet;
        this.averageVelocityBeforeIntersection = builder.averageVelocityBeforeIntersection;
        this.numberOfDetectedCollisions = builder.numberOfDetectedCollisions;
    }
}
