package tools.tsp;


import game.Position;

import java.util.Arrays;

public class TSPMain {
    
    public static int MAX_GENERATIONS = 10000;
    
    public static void main(String[] args) {
        
        // Create cities
        int numCities = 100;
        Position[] cities = new Position[numCities];
        
        // Loop to create random cities
        for (int cityIndex = 0; cityIndex < numCities; cityIndex++) {
            // Generate x,y position
            int xPos = (int) (100 * Math.random());
            int yPos = (int) (100 * Math.random());
            
            // Add city
            cities[cityIndex] = new Position(xPos, yPos);
        }
    
        // Create DM
        DistanceMatrix dm = new DistanceMatrix(Arrays.asList(cities));
        
        // Initial GA
        GeneticAlgorithm ga = new GeneticAlgorithm(100, 0.001, 0.9, 2, 5);
        
        // Initialize population
        Population population = ga.initPopulation(cities.length);
        
        System.out.println("Start Distance: " + population.get(0).getRouteLength(dm));
        
        // Keep track of current generation
        int generation = 1;
        // Start evolution loop
        while (generation < MAX_GENERATIONS) {
            population.sort(dm);
            System.out.println("G"+generation+" Best distance: " + population.get(0).getRouteLength(dm));
            
            // Apply crossover
            population = ga.crossoverPopulation(population, dm, numCities);
            
            // Apply mutation
            ga.mutatePopulation(population, dm);
            
            // Increment the current generation
            generation++;
        }
        
        System.out.println("Stopped after " + MAX_GENERATIONS + " generations.");
        population.sort(dm);
        Route route = population.get(0);
        System.out.println("Best distance: " + route.getRouteLength(dm));
        
    }
    
}