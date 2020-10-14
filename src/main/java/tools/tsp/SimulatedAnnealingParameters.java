package tools.tsp;

public class SimulatedAnnealingParameters {
    public double startTemp;
    public double endTemp;
    public double cooling;
    public int iterations;

    public SimulatedAnnealingParameters(double startTemp, double endTemp, double cooling, int iterations) {
        this.startTemp = startTemp;
        this.endTemp = endTemp;
        this.cooling = cooling;
        this.iterations = iterations;
    }
}
