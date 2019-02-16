package tools.tsp;

import emulator.SuperCC;
import game.Position;
import graphics.GameGraphicPosition;
import graphics.FullscreenGamePanel;
import graphics.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TSPGUI {
    private JPanel panel1;
    private JList nodeList;
    private JButton removeButton;
    private JButton solveButton;
    private List<Position> list;
    
    private MouseListener gameMouseListener;
    
    private static String[] listToString(List<? extends Object> l) {
        String[] out = new String[l.size()];
        for (int i = 0; i < l.size(); i++) out[i] = l.get(i).toString();
        return out;
    }
    
    public TSPGUI(SuperCC emulator){
        list = new LinkedList<Position>();
        list.add(emulator.getLevel().getChip().getPosition());
        nodeList.setListData(listToString(list));
        nodeList.setSelectedIndex(0);
        JFrame frame = new JFrame("TSP Solver");
        frame.setContentPane(panel1);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
        GamePanel gamePanel = emulator.getMainWindow().getGamePanel();
        gameMouseListener = gamePanel.getMouseListeners()[0];
        gamePanel.removeMouseListener(gamePanel.getMouseListeners()[0]);
        gamePanel.addMouseListener(new TSPGamePanelListener(emulator));
        frame.addWindowListener(new TSPWindowListener(emulator));
        removeButton.addActionListener((e) -> {
            int listIndex = nodeList.getSelectedIndex();
            list.remove(listIndex);
            nodeList.setListData(listToString(list));
            nodeList.setSelectedIndex(listIndex);
        });
        solveButton.addActionListener(new SolveActionListener(emulator, 150, 0.001, 0.9, 2, 5, 10000));
    }
    
    private class SolveActionListener implements ActionListener {
        private SuperCC emulator;
        private int populationSize, elitismCount, tournamentSize, generations;
        private double mutationRate, crossoverRate;
        
        @Override
        public void actionPerformed(ActionEvent e) {
    
            DistanceMatrix dm = new DistanceMatrix(list);
            GeneticAlgorithm ga = new GeneticAlgorithm(populationSize, mutationRate, crossoverRate, elitismCount, tournamentSize);
            Population population = ga.initPopulation(list.size());
    
            System.out.println("Start Distance: " + population.get(0).getRouteLength(dm));
    
            // Keep track of current generation
            int generation = 1;
            // Start evolution loop
            while (generation < generations) {
                population.sort(dm);
                System.out.println("G"+generation+" Best distance: " + population.get(0).getRouteLength(dm));
        
                // Apply crossover
                population = ga.crossoverPopulation(population, dm, list.size());
        
                // Apply mutation
                ga.mutatePopulation(population, dm);
        
                // Increment the current generation
                generation++;
            }
            
            population.sort(dm);
            Route route = population.get(0);
            GamePanel gp = emulator.getMainWindow().getGamePanel();
            ArrayList<Position> routePositions = new ArrayList<>(route.getChromosome().length);
            for (int i = 0; i < populationSize; i++) {
                System.out.println(population.get(i).getFitness(dm));
                System.out.println(population.get(i).getRouteLength(dm));
            }
            for (int i : route.getChromosome()) {
                routePositions.add(list.get(i));
            }
            gp.drawPositionList(routePositions, (Graphics2D) gp.getGraphics());
        }
        
        public SolveActionListener(SuperCC emulator, int populationSize, double mutationRate, double crossoverRate,
                                   int elitismCount, int tournamentSize, int generations) {
            this.emulator = emulator;
            this.populationSize = populationSize;
            this.elitismCount = elitismCount;
            this.tournamentSize = tournamentSize;
            this.generations = generations;
            this.mutationRate = mutationRate;
            this.crossoverRate = crossoverRate;
        }
    }
    
    private void createUIComponents() {
        nodeList = new JList();
    }
    
    private class TSPGamePanelListener implements MouseListener {
        private SuperCC emulator;
        @Override
        public void mouseClicked(MouseEvent e) {
            GameGraphicPosition clickPosition = new GameGraphicPosition(e, -1, -1);
            emulator.showAction("Clicked " + clickPosition);
            emulator.getMainWindow().getGamePanel().repaint();
            int listIndex = nodeList.getSelectedIndex() + 1;
            list.add(listIndex, clickPosition);
            nodeList.setListData(listToString(list));
            nodeList.setSelectedIndex(listIndex);
        }
        
        @Override
        public void mousePressed(MouseEvent e) {}
        
        @Override
        public void mouseReleased(MouseEvent e) {}
        
        @Override
        public void mouseEntered(MouseEvent e) {}
        
        @Override
        public void mouseExited(MouseEvent e) {}
        
        public TSPGamePanelListener(SuperCC emulator) {
            this.emulator = emulator;
        }
    }
    
    private class TSPWindowListener implements WindowListener {
        private SuperCC emulator;
        @Override
        public void windowOpened(WindowEvent e) {}
    
        @Override
        public void windowClosing(WindowEvent e) {
            GamePanel gp = emulator.getMainWindow().getGamePanel();
            gp.removeMouseListener(gp.getMouseListeners()[0]);
            gp.addMouseListener(gameMouseListener);
        }
    
        @Override
        public void windowClosed(WindowEvent e) {}
    
        @Override
        public void windowIconified(WindowEvent e) {}
    
        @Override
        public void windowDeiconified(WindowEvent e) {}
    
        @Override
        public void windowActivated(WindowEvent e) {}
    
        @Override
        public void windowDeactivated(WindowEvent e) {}
    
        public TSPWindowListener(SuperCC emulator) {
            this.emulator = emulator;
        }
    }
    
}
