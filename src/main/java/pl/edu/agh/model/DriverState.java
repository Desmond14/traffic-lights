package pl.edu.agh.model;

public class DriverState {
    private final Street street;
    private final Integer positionOnStreet;
    private final Integer currentVelocity;

    public DriverState(Street street, Integer positionOnStreet, Integer currentVelocity) {
        this.street = street;
        this.positionOnStreet = positionOnStreet;
        this.currentVelocity = currentVelocity;
    }

    public Street getStreet() {
        return street;
    }

    public Integer getPositionOnStreet() {
        return positionOnStreet;
    }

    public Integer getCurrentVelocity() {
        return currentVelocity;
    }
}
