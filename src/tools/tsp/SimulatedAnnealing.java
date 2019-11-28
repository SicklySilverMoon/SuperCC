package tools.tsp;

import java.util.Random;

public class SimulatedAnnealing {
    private double temparature;
    private double end;
    private double cooling;
    private int iterations;
    private int[][] distances;
    Random r = new Random();
    private int[] bestSolution;
    private int bestDistance;

    public SimulatedAnnealing(double temparature, double end, double cooling, int iterations, int[][] distances) {
        this.temparature = temparature;
        this.end = end;
        this.cooling = cooling;
        this.iterations = iterations;
        this.distances = distances;
        this.bestSolution = initialSolution();
        this.bestDistance = calculateDistance(bestSolution);
    }

    public int[] start() {
        if(distances.length == 2) {
            return new int[]{};
        }
        if(distances.length == 3) {
            return new int[]{1};
        }

        int[] solution = bestSolution.clone();
        int distance = bestDistance;

        while(temparature > end) {
            temparature *= cooling;
            int startDistance = distance;
            for(int i = 0; i < iterations; i++) {
                int[] newSolution = solution.clone();
                mutate(newSolution);
                int newDistance = calculateDistance(newSolution);

                if(newDistance < distance) {
                    distance = newDistance;
                    solution = newSolution.clone();
                }
                else if(Math.exp(((double)distance - newDistance)/temparature) > r.nextDouble()) {
                    distance = newDistance;
                    solution = newSolution.clone();
                }

                if(newDistance < bestDistance) {
                    bestDistance = newDistance;
                    bestSolution = newSolution.clone();
                    System.out.println(temparature);
                    System.out.println(bestDistance);
                }
            }
            if(distance < startDistance) {
                temparature /= cooling;
            }
            //System.out.println(temparature);
        }
        return bestSolution;
    }

    private int[] initialSolution() {
        int[] solution = new int[distances.length - 2];
        for(int i = 0; i < distances.length - 2; i++) {
            solution[i] = i + 1;
        }

        for(int i = solution.length - 1; i > 0; i--) {
            int index = r.nextInt(i + 1);
            int temp = solution[index];
            solution[index] = solution[i];
            solution[i] = temp;
        }

        return solution;
    }

    private int calculateDistance(int[] solution) {
        int distance = distances[0][solution[0]];
        for(int i = 0; i < solution.length - 1; i++) {
            distance += distances[solution[i]][solution[i + 1]];
        }
        distance += distances[solution[solution.length - 1]][solution.length + 1];
        return distance;
    }

    private void mutate(int[] solution) {
        int index1 = r.nextInt(solution.length);
        int index2 = index1;
        while(index1 == index2) {
            index2 = r.nextInt(solution.length);
        }
        if(index1 > index2) {
            int temp = index1;
            index1 = index2;
            index2 = temp;
        }

        int type = r.nextInt(3);

        if(type == 0) {
            int temp = solution[index1];
            solution[index1] = solution[index2];
            solution[index2] = temp;
        }
        else if(type == 1) {
            int temp = solution[index1];
            for(int i = index1 + 1; i <= index2; i++) {
                solution[i - 1] = solution[i];
            }
            solution[index2] = temp;
        }
        else if(type == 2) {
            for(int i = index1; i <= (index1 + index2)/2; i++) {
                int temp = solution[i];
                solution[i] = solution[solution.length - i - 1];
                solution[solution.length - i - 1] = temp;
            }
        }
    }
}
