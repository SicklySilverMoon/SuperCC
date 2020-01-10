package tools.tsp;

import tools.TSPGUI;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Random;

public class SimulatedAnnealing {
    TSPGUI gui;
    private double temparature;
    private double end;
    private double cooling;
    private int iterations;
    private int[][] distances;
    private int[][] distancesBoost;
    private boolean[][] boostNodes;
    private boolean[][] boostNodesBoost;
    Random r = new Random();
    private int[] bestSolution;
    private int bestDistance;
    private int inputNodeSize;
    private int exitNodeSize;
    public int bestExit;
    private int exitChosen;
    private ArrayList<TSPGUI.RestrictionNode> restrictionNodes;
    private JTextPane output;

    public SimulatedAnnealing(TSPGUI gui, double temparature, double end, double cooling, int iterations, int[][] distances,
                              int[][] distancesBoost, boolean[][] boostNodes, boolean[][] boostNodesBoost,
                              int inputNodeSize, int exitNodeSize, ArrayList<TSPGUI.RestrictionNode> restrictionNodes, JTextPane output) {
        this.gui = gui;
        this.temparature = temparature;
        this.end = end;
        this.cooling = cooling;
        this.iterations = iterations;
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

        while(temparature > end && !gui.killFlag) {
            temparature *= cooling;
            output.setText("Calculating shortest path...\nTemperature: " + temparature + "\nCurrent best: " + bestDistance);
            int startDistance = distance;
            for(int i = 0; i < iterations; i++) {
                int[] newSolution = solution.clone();
                mutate(newSolution);
                if(!checkRestrictions(newSolution)) {
                    solution = newSolution.clone();
                    continue;
                }
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
                    bestExit = exitChosen;
                }
            }
            if(distance < startDistance) {
                temparature /= cooling;
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
            int temp = solution[index];
            solution[index] = solution[i];
            solution[i] = temp;
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

    private boolean checkRestrictions(int[] solution) {
        boolean[] restrictionsMet = new boolean[restrictionNodes.size()];
        int[] indexes = new int[inputNodeSize];

        for(int i = 0; i < solution.length; i++) {
            indexes[solution[i] - 1] = i;
        }

        for(int i = 0; i < restrictionNodes.size(); i++) {
            TSPGUI.RestrictionNode node = restrictionNodes.get(i);
            restrictionsMet[i] = indexes[node.beforeIndex] < indexes[node.afterIndex];
            if(!restrictionsMet[i]) return false;
        }

        return true;
    }
}
