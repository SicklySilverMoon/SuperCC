package tools.tsp;

public class ActingWallParameters {
    public boolean isWaterWall;
    public boolean isFireWall;
    public boolean isBombWall;
    public boolean isThiefWall;
    public boolean isTrapWall;

    public ActingWallParameters(boolean isWaterWall, boolean isFireWall, boolean isBombWall, boolean isThiefWall, boolean isTrapWall) {
        this.isWaterWall = isWaterWall;
        this.isFireWall = isFireWall;
        this.isBombWall = isBombWall;
        this.isThiefWall = isThiefWall;
        this.isTrapWall = isTrapWall;
    }
}
