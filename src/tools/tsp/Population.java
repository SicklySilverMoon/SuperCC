package tools.tsp;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class Population {
    
    private Route[] population;
    private double populationFitness = -1;
    
    public void calculatePopulationFitness(DistanceMatrix dm) {
        double sum = 0;
        for (Route route : population) {
            sum += route.getFitness(dm);
        }
        populationFitness = sum / (double) population.length;
    }
    
    public double getPopulationFitness(DistanceMatrix dm) {
        if (populationFitness < 0) calculatePopulationFitness(dm);
        return populationFitness;
    }
    
    public int size() {
        return population.length;
    }
    
    public Route set(int index, Route route) {
        return population[index] = route;
    }
    
    public Route get(int index) {
        return population[index];
    }
    
    public void sort(DistanceMatrix dm) {
        Arrays.sort(this.population, (i1, i2) -> {
            double f1 = i1.getFitness(dm), f2 = i2.getFitness(dm);
            if (f1 > f2) {
                return -1;
            } else if (f1 < f2) {
                return 1;
            }
            return 0;
        });
    }
    
    public void shuffle() {
        for (int i = population.length - 1; i > 0; i--) {
            int swapIndex = ThreadLocalRandom.current().nextInt(i + 1);
            Route swap = population[swapIndex];
            population[swapIndex] = population[i];
            population[i] = swap;
        }
    }
    
    public Population(int populationSize, int chromosomeLength) {
        this.population = new Route[populationSize];
        for (int i = 0; i < populationSize; i++) {
            this.population[i] = new Route(chromosomeLength);
        }
    }
    
    public Population(Route[] population) {
        this.population = population;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Route i : population) {
            sb.append(i.toString());
            sb.append('\n');
        }
        return sb.toString();
    }
    
}