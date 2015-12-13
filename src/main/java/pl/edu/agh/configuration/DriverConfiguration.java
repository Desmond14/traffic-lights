package pl.edu.agh.configuration;

import static com.google.common.base.MoreObjects.toStringHelper;

public class DriverConfiguration {
    public final Integer maxVelocity;
    public final Integer acceleration;
    public final Integer carLength;
    public final Integer carWidth;
    public final Integer initialDistanceToIntersection;
    public final Float yellowLightGoProbability;

    public static class Builder {
        private Integer maxVelocity;
        private Integer acceleration;
        private Integer carLength;
        private Integer carWidth;
        private Integer initialDistanceToIntersection;
        private Float yellowLightGoProbability;

        public Builder maxVelocity(Integer maxVelocity) {
            this.maxVelocity = maxVelocity;
            return this;
        }

        public Builder acceleration(Integer acceleration) {
            this.acceleration = acceleration;
            return this;
        }

        public Builder carLength(Integer carLength) {
            this.carLength = carLength;
            return this;
        }

        public Builder carWidth(Integer carWidth) {
            this.carWidth = carWidth;
            return this;
        }

        public Builder initialDistanceToIntersection(Integer initialDistanceToIntersection) {
            this.initialDistanceToIntersection = initialDistanceToIntersection;
            return this;
        }

        public Builder yellowLightGoProbability(Float yellowLightGoProbability) {
            this.yellowLightGoProbability = yellowLightGoProbability;
            return this;
        }

        public DriverConfiguration build() {
            return new DriverConfiguration(this);
        }
    }

    @Override
    public String toString() {
        return toStringHelper(this.getClass())
                .add("maxVelocity", maxVelocity)
                .add("acceleration", acceleration)
                .add("carLength", carLength)
                .add("carWidth", carWidth)
                .add("initialDistanceToIntersection", initialDistanceToIntersection)
                .add("yellowGoProbability", yellowLightGoProbability)
                .toString();
    }

    private DriverConfiguration(Builder builder) {
        maxVelocity = builder.maxVelocity;
        acceleration = builder.acceleration;
        carLength = builder.carLength;
        carWidth = builder.carWidth;
        initialDistanceToIntersection = builder.initialDistanceToIntersection;
        yellowLightGoProbability = builder.yellowLightGoProbability;
    }
}
