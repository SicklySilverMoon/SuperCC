package tools.tsp;

import java.util.concurrent.ThreadLocalRandom;

public class Route {

    private int[] chromosome;
    private double fitness = -1;
    private int routeLength = -1;
    
    public int[] getChromosome() {
        return this.chromosome;
    }
    
    public int getChromosomeLength() {
        return chromosome.length;
    }
    
    public void set(int index, int newValue) {
        this.chromosome[index] = newValue;
    }
    
    public int get(int index) {
        return this.chromosome[index];
    }
    
    private void calculateRouteLength(DistanceMatrix dm) {
        routeLength = 0;
        int previousGene = chromosome[0];
        for (int gene : chromosome) {
            routeLength += dm.getDistance(previousGene, gene);
            previousGene = gene;
        }
    }
    
    public int getRouteLength(DistanceMatrix dm) {
        calculateRouteLength(dm);
        return routeLength;
    }
    
    private void calculateFitness(DistanceMatrix dm) {
        fitness = 1.0 / (double) getRouteLength(dm);
    }
    
    public double getFitness(DistanceMatrix dm) {
        calculateFitness(dm);
        return fitness;
    }
    
    public boolean contains(int gene) {
        for (int n : chromosome) {
            if (n == gene) return true;
        }
        return false;
    }
    
    // Shuffles all elements in the array except the first and the last
    private static void shuffleMiddle(int[] a) {
        for (int i = a.length - 2; i > 1; i--) {
            int swapIndex = 1 + ThreadLocalRandom.current().nextInt(i - 1);
            int swap = a[swapIndex];
            a[swapIndex] = a[i];
            a[i] = swap;
        }
    }
    
    public Route(int[] chromosome) {
        this.chromosome = chromosome;
    }
    
    public Route(int chromosomeLength) {
        chromosome = new int[chromosomeLength];
        for (int i = 0; i < chromosomeLength; i++) {
            chromosome[i] = i;
        }
        shuffleMiddle(chromosome);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int gene : chromosome) sb.append(gene + ", ");
        return sb.toString();
    }
    
}
