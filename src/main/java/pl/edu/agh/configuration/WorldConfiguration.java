package pl.edu.agh.configuration;

import pl.edu.agh.model.Street;

import java.util.Map;

public class WorldConfiguration {
    public final Integer monitoredDistanceFromCrossing;
    public final Integer streetWidth;
    public final Boolean useSimpleLights;
    public final Map<Street, Float> newCarGenerationProbability;
    public final Integer simulationIterations;

    public static class Builder {
        private Integer monitoredDistanceFromCrossing;
        private Integer streetWidth;
        private Boolean useSimpleLights;
        private Map<Street, Float> newCarGenerationProbability;
        private Integer simulationIterations;

        public Builder monitoredDistanceFromCrossing(Integer monitoredDistanceFromCrossing) {
            this.monitoredDistanceFromCrossing = monitoredDistanceFromCrossing;
            return this;
        }

        public Builder streetWidth(Integer streetWidth) {
            this.streetWidth = streetWidth;
            return this;
        }

        public Builder useSimpleLights(Boolean useSimpleLights) {
            this.useSimpleLights = useSimpleLights;
            return this;
        }

        public Builder newCarGenerationProbability(Map<Street, Float> newCarGenerationProbability) {
            this.newCarGenerationProbability = newCarGenerationProbability;
            return this;
        }

        public Builder simulationIterations(Integer simulationIterations) {
            this.simulationIterations = simulationIterations;
            return this;
        }

        public WorldConfiguration build() {
            return new WorldConfiguration(this);
        }

    }

    private WorldConfiguration(Builder builder) {
        this.monitoredDistanceFromCrossing = builder.monitoredDistanceFromCrossing;
        this.streetWidth = builder.streetWidth;
        this.useSimpleLights = builder.useSimpleLights;
        this.newCarGenerationProbability = builder.newCarGenerationProbability;
        this.simulationIterations = builder.simulationIterations;
    }

}
