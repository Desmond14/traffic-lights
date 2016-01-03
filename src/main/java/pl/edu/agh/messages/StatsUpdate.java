package pl.edu.agh.messages;

import pl.edu.agh.model.WorldSnapshot;

public class StatsUpdate {
    public final int detectedCollisions;
    public final WorldSnapshot previousSnapshot;
    public final WorldSnapshot currentSnapshot;

    public StatsUpdate(int detectedCollisions, WorldSnapshot previousSnapshot, WorldSnapshot currentSnapshot) {
        this.detectedCollisions = detectedCollisions;
        this.previousSnapshot = previousSnapshot;
        this.currentSnapshot = currentSnapshot;
    }
}
