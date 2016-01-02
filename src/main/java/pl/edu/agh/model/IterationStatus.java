package pl.edu.agh.model;

public final class IterationStatus {
    private int carsInIteration = 0;
    private int iterationNo = 0;

    private int driverUpdatesCounter = 0;
    private int detectedCollisionsCounter = 0;
    private boolean trafficLightsUpdateReceived = false;
    private boolean trafficGenerationUpdateReceived = false;

    public void startNewIteration(int carsInIteration) {
        this.carsInIteration = carsInIteration;
        iterationNo++;
        detectedCollisionsCounter = 0;
        driverUpdatesCounter = 0;
        trafficGenerationUpdateReceived = false;
        trafficLightsUpdateReceived = false;
    }

    public int incrementDriverUpdatesCounter() {
        return ++driverUpdatesCounter;
    }

    public int getIterationNo() {
        return iterationNo;
    }

    public int getDetectedCollisionsCounter() {
        return detectedCollisionsCounter;
    }

    public int incrementDetectedCollisionsCounter() {
        return ++detectedCollisionsCounter;
    }

    public void markTrafficLightsUpdateReceived() {
        this.trafficLightsUpdateReceived = true;
    }

    public boolean isTrafficLightsUpdateReceived() {
        return trafficLightsUpdateReceived;
    }

    public void markTrafficGenerationUpdateReceived() {
        this.trafficGenerationUpdateReceived = true;
    }

    public boolean isTrafficGenerationUpdateReceived() {
        return trafficGenerationUpdateReceived;
    }

    public int getDriverUpdatesCounter() {
        return driverUpdatesCounter;
    }

    public int getCarsInIteration() {
        return carsInIteration;
    }

    public boolean areAllUpdatesReceived() {
        return carsInIteration == driverUpdatesCounter && trafficGenerationUpdateReceived && trafficLightsUpdateReceived;
    }
}
