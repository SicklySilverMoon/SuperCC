package tools.tsp;

import game.Position;

import java.util.List;

public class DistanceMatrix {
    
    private int[][] distances;
    
    private static int calculateDistance(Position p1, Position p2) {
        return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY() - p2.getY());
    }
    
    public int getDistance(int from, int to) {
        return distances[from][to];
    }
    
    public DistanceMatrix(List<Position> positions) {
        int size = positions.size();
        distances = new int[size][size];
        for (int i = 0; i < size; i++) {
            Position p1 = positions.get(i);
            for (int j = 0; j < size; j++) {
                distances[i][j] = calculateDistance(p1, positions.get(j));
            }
        }
    }

}
