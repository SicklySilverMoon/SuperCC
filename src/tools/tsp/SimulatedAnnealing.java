package tools.tsp;

import tools.TSPGUI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {
    private TSPGUI gui;
    private double temperature;
    private double end;
    private double cooling;
    private int iterations;
    private int[][] distances;
    private int[][] distancesBoost;
    private boolean[][] boostNodes;
    private boolean[][] boostNodesBoost;
    private int[] bestSolution;
    private int bestDistance;
    private int inputNodeSize;
    private int exitNodeSize;
    public int bestExit;
    private int exitChosen;
    private int startTime;
    private ArrayList<TSPGUI.RestrictionNode> restrictionNodes;
    private JTextPane output;
    private Random r = new Random();

    public SimulatedAnnealing(TSPGUI gui, int startTime, SimulatedAnnealingParameters simulatedAnnealingParameters, int[][] distances,
                              int[][] distancesBoost, boolean[][] boostNodes, boolean[][] boostNodesBoost,
                              int inputNodeSize, int exitNodeSize, ArrayList<TSPGUI.RestrictionNode> restrictionNodes, JTextPane output) {
        this.gui = gui;
        this.startTime = startTime <= 0 ? 9999 : startTime;
        this.temperature = simulatedAnnealingParameters.startTemp;
        this.end = simulatedAnnealingParameters.endTemp;
        this.cooling = simulatedAnnealingParameters.cooling;
        this.iterations = simulatedAnnealingParameters.iterations;
        this.distances = distances;
        this.distancesBoost = distancesBoost;
        this.boostNodes = boostNodes;
        this.boostNodesBoost = boostNodesBoost;
        this.inputNodeSize = inputNodeSize;
        this.exitNodeSize = exitNodeSize;
        this.bestSolution = initialSolution();
        this.bestDistance = Integer.MAX_VALUE;
        this.restrictionNodes = restrictionNodes;
        this.output = output;
    }

    public int[] start() {
        bestExit = exitChosen;

        if(inputNodeSize == 0) {
            return new int[]{};
        }
        if(inputNodeSize == 1) {
            return new int[]{1};
        }

        int[] solution = bestSolution.clone();
        int distance = bestDistance;

        double totalSteps = Math.ceil(Math.log(end/temperature)/Math.log(cooling));
        double steps = 0;

        while(temperature > end && !gui.killFlag) {
            temperature *= cooling;
            output.setText("Calculating shortest path..." +
                    "\nProgress: " + (steps/totalSteps*100) + "%" +
                    "\nTemperature: " + temperature +
                    "\nCurrent best: " + ((double)(startTime - bestDistance + 2)/10));
            int startDistance = distance;
            for(int i = 0; i < iterations; i++) {
                int[] newSolution = solution.clone();
                mutate(newSolution);
                handleRestrictions(newSolution);

                int newDistance = calculateDistance(newSolution);
                if(newDistance < distance) {
                    distance = newDistance;
                    solution = newSolution.clone();
                }
                else if(shouldAccept(distance, newDistance)) {
                    distance = newDistance;
                    solution = newSolution.clone();
                }

                if(newDistance < bestDistance) {
                    bestDistance = newDistance;
                    bestSolution = newSolution.clone();
                    bestExit = exitChosen;
                }
            }
            if(distance < startDistance) {
                temperature /= cooling;
            } else {
                steps++;
            }
        }
        return bestSolution;
    }

    private int[] initialSolution() {
        int[] solution = new int[inputNodeSize];
        for(int i = 0; i < inputNodeSize; i++) {
            solution[i] = i + 1;
        }

        for(int i = solution.length - 1; i > 0; i--) {
            int index = r.nextInt(i + 1);
            swap(solution, i, index);
        }

        return solution;
    }

    private int calculateDistance(int[] solution) {
        int distance = distances[0][solution[0]];
        boolean boosted = false;
        for(int i = 0; i < solution.length - 1; i++) {
            distance += boosted ?
                    distancesBoost[solution[i]][solution[i + 1]] : distances[solution[i]][solution[i + 1]];
            boosted = boosted ?
                    boostNodesBoost[solution[i]][solution[i + 1]] : boostNodes[solution[i]][solution[i + 1]];
        }

        exitChosen = 0;
        int bestExitDistance = distances[solution[solution.length - 1]][solution.length + 1];
        for(int i = 1; i < exitNodeSize; i++) {
            if(distances[solution[solution.length - 1]][solution.length + 1 + i] < bestExitDistance) {
                exitChosen = i;
                if(boosted) {
                    bestExitDistance = distancesBoost[solution[solution.length - 1]][solution.length + 1 + i];
                }
                else {
                    bestExitDistance = distances[solution[solution.length - 1]][solution.length + 1 + i];
                }
            }
        }
        distance += bestExitDistance;
        if(distance < 0) return 9999;
        return distance;
    }

    private void mutate(int[] solution) {
        int index1 = r.nextInt(solution.length);
        int index2 = index1;
        int temp;
        while(index1 == index2) {
            index2 = r.nextInt(solution.length);
        }
        if(index1 > index2) {
            temp = index1;
            index1 = index2;
            index2 = temp;
        }

        int type = r.nextInt(3);

        switch(type) {
            case 0:
                temp = solution[index1];
                for(int i = index1 + 1; i <= index2; i++) {
                    solution[i - 1] = solution[i];
                }
                solution[index2] = temp;
                break;
            case 1:
                opt2(solution, index1, index2);
                break;
            case 2:
                int index3 = index2;
                while(index3 == index1 || index3 == index2) {
                    index3 = r.nextInt(solution.length + 1);
                }
                if(index3 < index1) {
                    temp = index3;
                    index3 = index2;
                    index2 = index1;
                    index1 = temp;
                }
                else if(index3 < index2) {
                    temp = index3;
                    index3 = index2;
                    index2 = temp;
                }
                int opt3Type = r.nextInt(4);
                opt3(solution, index1, index2, index3, opt3Type);
        }
    }

    private void opt2(int[] solution, int index1, int index2) {
        for(int i = index1; i <= (index1 + index2)/2; i++) {
            swap(solution, i, index1 + index2 - i);
        }
    }

    private void opt3(int[] solution, int index1, int index2, int index3, int type) {
        switch(type) {
            case 0:
                for(int i = index1; i < Math.floor((index1+index2)/2); i++) swap(solution, i, index1 + index2 - i - 1);
                for(int i = index2; i < Math.floor((index2+index3)/2); i++) swap(solution, i, index2 + index3 - i - 1);
                for(int i = index1; i < Math.floor((index1+index3)/2); i++) swap(solution, i, index1 + index3 - i - 1);
                break;
            case 1:
                for(int i = index2; i < Math.floor((index2+index3)/2); i++) swap(solution, i, index2 + index3 - i - 1);
                for(int i = index1; i < Math.floor((index1+index3)/2); i++) swap(solution, i, index1 + index3 - i - 1);
                break;
            case 2:
                for(int i = index1; i < Math.floor((index1+index2)/2); i++) swap(solution, i, index1 + index2 - i - 1);
                for(int i = index1; i < Math.floor((index1+index3)/2); i++) swap(solution, i, index1 + index3 - i - 1);
                break;
            case 3:
                for(int i = index1; i < Math.floor((index1+index2)/2); i++) swap(solution, i, index1 + index2 - i - 1);
                for(int i = index2; i < Math.floor((index2+index3)/2); i++) swap(solution, i, index2 + index3 - i - 1);
        }
    }

    private void handleRestrictions(int[] solution) {
        int[] indexes = new int[inputNodeSize];

        for(int i = 0; i < solution.length; i++) {
            indexes[solution[i] - 1] = i;
        }

        for(int i = 0; i < restrictionNodes.size(); i++) {
            TSPGUI.RestrictionNode node = restrictionNodes.get(i);
            if(indexes[node.beforeIndex] > indexes[node.afterIndex]) {
                swap(solution, indexes[node.beforeIndex], indexes[node.afterIndex]);
            }
        }
    }

    private boolean shouldAccept(int distance, int newDistance) {
        return Math.exp(((double)distance - newDistance)/ temperature) > r.nextDouble();
    }

    private void swap(int[] solution, int i, int j) {
        int temp = solution[i];
        solution[i] = solution[j];
        solution[j] = temp;
    }
}
