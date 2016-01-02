package pl.edu.agh.configuration;

public class TrafficLightsConfiguration {
    public final Integer counterLimitValue;
    public final Integer shortSupervisedDistance;
    public final Integer shortSupervisedDistanceMaxCarsNo;
    public final Integer longSupervisedDistance;
    public final Integer minimumGreenTime;
    public final Integer yellowLightDuration;
    public final Integer northSouthGreenLightDuration;
    public final Integer westEastGreenLightDuration;

    public static class Builder {
        private Integer counterLimitValue;
        private Integer shortSupervisedDistance;
        private Integer shortSupervisedDistanceMaxCarsNo;
        private Integer longSupervisedDistance;
        private Integer minimumGreenTime;
        private Integer yellowLightDuration;
        private Integer northSouthGreenLightDuration;
        private Integer westEastGreenLightDuration;

        public Builder counterLimitValue(Integer counterLimitValue) {
            this.counterLimitValue = counterLimitValue;
            return this;
        }

        public Builder shortSupervisedDistance(Integer shortSupervisedDistance) {
            this.shortSupervisedDistance = shortSupervisedDistance;
            return this;
        }

        public Builder shortSupervisedDistanceMaxCarsNo(Integer shortSupervisedDistanceMaxCarsNo) {
            this.shortSupervisedDistanceMaxCarsNo = shortSupervisedDistanceMaxCarsNo;
            return this;
        }

        public Builder longSupervisedDistance(Integer longSupervisedDistance) {
            this.longSupervisedDistance = longSupervisedDistance;
            return this;
        }

        public Builder minimumGreenTime(Integer minimumGreenTime) {
            this.minimumGreenTime = minimumGreenTime;
            return this;
        }

        public Builder yellowLightDuration(Integer yellowLightDuration) {
            this.yellowLightDuration = yellowLightDuration;
            return this;
        }

        public Builder northSouthGreenLightDuration(Integer northSouthGreenLightDuration) {
            this.northSouthGreenLightDuration = northSouthGreenLightDuration;
            return this;
        }

        public Builder westEastGreenLightDuration(Integer westEastGreenLightDuration) {
            this.westEastGreenLightDuration = westEastGreenLightDuration;
            return this;
        }

        public TrafficLightsConfiguration build() {
            return new TrafficLightsConfiguration(this);
        }

    }

    private TrafficLightsConfiguration(Builder builder) {
        this.counterLimitValue = builder.counterLimitValue;
        this.shortSupervisedDistance = builder.shortSupervisedDistance;
        this.shortSupervisedDistanceMaxCarsNo = builder.shortSupervisedDistanceMaxCarsNo;
        this.longSupervisedDistance = builder.longSupervisedDistance;
        this.minimumGreenTime = builder.minimumGreenTime;
        this.yellowLightDuration = builder.yellowLightDuration;
        this.northSouthGreenLightDuration = builder.northSouthGreenLightDuration;
        this.westEastGreenLightDuration = builder.westEastGreenLightDuration;
    }

}
