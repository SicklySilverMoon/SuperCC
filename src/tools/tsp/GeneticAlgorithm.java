package tools.tsp;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticAlgorithm {
    
    private int populationSize, tournamentSize, elitismCount;
    private double mutationRate, crossoverRate;
    
    public GeneticAlgorithm(int populationSize, double mutationRate, double crossoverRate, int elitismCount,
                            int tournamentSize) {
        this.populationSize = populationSize;
        this.mutationRate = mutationRate;
        this.crossoverRate = crossoverRate;
        this.elitismCount = elitismCount;
        this.tournamentSize = tournamentSize;
    }
    
    public Population initPopulation(int chromosomeLength){
        System.out.println(new Population(this.populationSize, chromosomeLength));
        return new Population(this.populationSize, chromosomeLength);
    }
    
    public Route selectParent(Population population, DistanceMatrix dm) {
        int min = populationSize;
        for (int i = 0; i < this.tournamentSize; i++) {
            int n = ThreadLocalRandom.current().nextInt(this.tournamentSize-1);
            if (n < min) min = n;
        }
        return population.get(min);
    }
    
    public Population crossoverPopulation(Population population, DistanceMatrix dm, int chromosomeLength){
        Population newPopulation = new Population(population.size(), chromosomeLength);
        
        population.sort(dm);
        for (int childIndex = 0; childIndex < population.size(); childIndex++) {
            
            Route parent1 = population.get(childIndex);
            if (childIndex >= elitismCount && crossoverRate > Math.random()) {
                Route parent2 = this.selectParent(population, dm);
                int[] offspringChromosome = new int[chromosomeLength];
                Arrays.fill(offspringChromosome, -1);
                offspringChromosome[0] = 0;
                offspringChromosome[chromosomeLength-1] = chromosomeLength-1;
                Route offspring = new Route(offspringChromosome);
                
                // Get subset of parent chromosomes
                int a = 1 + ThreadLocalRandom.current().nextInt(chromosomeLength-1);
                int b = 1 + ThreadLocalRandom.current().nextInt(chromosomeLength-1);
                int startSubstr = Math.min(a, b);
                int endSubstr = Math.max(a, b);
                
                for (int i = startSubstr; i < endSubstr; i++) {
                    offspringChromosome[i] = parent1.get(i);
                }
                
                int parentGeneIndex = endSubstr;
                for (int i = 0; i < parent2.getChromosomeLength(); i++) {
                    int gene = parent2.get(parentGeneIndex);
                    if (!offspring.contains(gene)) {
                        for (int ii = 0; ii < offspring.getChromosomeLength(); ii++) {
                            if (offspring.get(ii) == -1) {
                                offspring.set(ii, gene);
                                break;
                            }
                        }
                    }
                    parentGeneIndex++;
                    if (parentGeneIndex >= chromosomeLength) parentGeneIndex -= chromosomeLength;
                }
                newPopulation.set(childIndex, offspring);
            } else {
                newPopulation.set(childIndex, parent1);
            }
        }
        
        return newPopulation;
    }
    
    public void mutatePopulation(Population population, DistanceMatrix dm){
        population.sort(dm);
        for (int i = elitismCount; i < population.size(); i++) {
            Route route = population.get(i);
            for (int j = 1; j < route.getChromosomeLength()-1; j++) {
                if (mutationRate > Math.random()) {
                    int newGenePos = 1 + ThreadLocalRandom.current().nextInt(route.getChromosomeLength()-2);
                    int swap = route.get(j);
                    route.set(j, route.get(newGenePos));
                    route.set(newGenePos, swap);
                }
            }
        }
    }
    
}