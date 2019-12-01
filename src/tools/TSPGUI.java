package tools;

import emulator.SuperCC;
import game.Position;
import game.Tile;
import tools.tsp.TSPSolver;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

public class TSPGUI {
    private JPanel mainPanel;
    private JList nodesList;
    private JList exitsList;
    private JList restrictionsList;
    private JTextField nodesInput;
    private JButton addNodeButton;
    private JTextField exitsInput;
    private JButton addExitButton;
    private JTextField restrictionsInput;
    private JButton addRestrictionButton;
    private JTextField startTempInput;
    private JTextField endTempInput;
    private JTextField coolingInput;
    private JButton quickButton;
    private JButton normalButton;
    private JButton longButton;
    private JButton thoroughButton;
    private JButton runButton;
    private JTextField iterationsInput;
    private JButton allChipsButton;
    private JButton allExitsButton;
    private JTextPane output;
    private JButton removeNodeButton;
    private JButton removeExitButton;
    private JButton removeRestrictionButton;
    private JCheckBox waterCheckBox;
    private JCheckBox fireCheckBox;
    private JCheckBox bombsCheckBox;
    private JCheckBox thievesCheckBox;
    private JCheckBox trapsCheckBox;

    private DefaultListModel<ListNode> nodes = new DefaultListModel<>();
    private DefaultListModel<ListNode> exitNodes = new DefaultListModel<>();
    private DefaultListModel<RestrictionNode> restrictionNodes = new DefaultListModel<>();

    private SuperCC emulator;

    public boolean killFlag = false;
    public static boolean running = false;

    public TSPGUI(SuperCC emulator) {
        this.emulator = emulator;

        this.nodesList.setModel(nodes);
        this.exitsList.setModel(exitNodes);
        this.restrictionsList.setModel(restrictionNodes);

        JFrame frame = new JFrame("TSP Solver");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowListener() {
            @Override public void windowClosing(WindowEvent windowEvent) { killFlag = true; }

            //None of these are useful but the code requires them to be here so i shoved them all into one line
            @Override public void windowOpened(WindowEvent windowEvent) {}@Override public void windowClosed(WindowEvent windowEvent) {}@Override public void windowIconified(WindowEvent windowEvent) {}@Override public void windowDeiconified(WindowEvent windowEvent) {}@Override public void windowActivated(WindowEvent windowEvent) { }@Override public void windowDeactivated(WindowEvent windowEvent) { }
        });

        runButton.addActionListener(e -> {
            if(running) {
                killFlag = true;
                return;
            }
            new TSPSolverThread(this).start();
        });


        addNodeButton.addActionListener(e -> {
            try {
                String[] coords = nodesInput.getText().split(" ");
                if(coords.length != 2) throw new Exception("Please enter two numbers between 0 and 31 separated by a space.");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                if(x < 0 || x > 31 || y < 0 || y > 31) throw new Exception("Numbers must be between 0 and 31 inclusive.");
                ListNode node = new ListNode(x, y, emulator.getLevel().getLayerFG().get(new Position(x, y)));
                if(checkDuplicate(nodes, node)) throw new Exception("No duplicates allowed");
                nodes.addElement(node);
                nodesInput.setText("");
            } catch(Exception ex) {
                emulator.throwError(ex.getMessage());
            }
        });

        removeNodeButton.addActionListener(e -> {
            int index = nodesList.getSelectedIndex();
            if(index != -1) {
                manageNodeRemoval(nodes.getElementAt(index));
                nodes.remove(index);
            }
        });

        allChipsButton.addActionListener(e -> {
            nodes.clear();
            restrictionNodes.clear();
            for(int i = 0; i < 32 * 32; i++) {
                Tile t = emulator.getLevel().getLayerFG().get(i);
                if(t == Tile.CHIP) {
                    nodes.addElement(new ListNode(i, t));
                }
            }
            for(int i = 0; i < 32 * 32; i++) {
                Tile t = emulator.getLevel().getLayerBG().get(i);
                if(t == Tile.CHIP) {
                    nodes.addElement(new ListNode(i, t));
                }
            }
        });


        addExitButton.addActionListener(e -> {
            try {
                String[] coords = exitsInput.getText().split(" ");
                if(coords.length != 2) throw new Exception("Please enter two numbers between 0 and 31 separated by a space.");
                int x = Integer.parseInt(coords[0]);
                int y = Integer.parseInt(coords[1]);
                if(x < 0 || x > 31 || y < 0 || y > 31) throw new Exception("Numbers must be between 0 and 31 inclusive.");
                ListNode node = new ListNode(x, y, emulator.getLevel().getLayerFG().get(new Position(x, y)));
                if(checkDuplicate(exitNodes, node)) throw new Exception("No duplicates allowed.");
                exitNodes.addElement(node);
                exitsInput.setText("");
            } catch(Exception ex) {
                emulator.throwError(ex.getMessage());
            }
        });

        removeExitButton.addActionListener(e -> {
            int index = exitsList.getSelectedIndex();
            if(index != -1) {
                exitNodes.remove(index);
            }
        });

        allExitsButton.addActionListener(e -> {
            exitNodes.clear();
            for(int i = 0; i < 32 * 32; i++) {
                Tile t = emulator.getLevel().getLayerFG().get(i);
                if(t == Tile.EXIT) {
                    exitNodes.addElement(new ListNode(i, t));
                }
            }
            for(int i = 0; i < 32 * 32; i++) {
                Tile t = emulator.getLevel().getLayerBG().get(i);
                if(t == Tile.EXIT) {
                    exitNodes.addElement(new ListNode(i, t));
                }
            }
        });


        addRestrictionButton.addActionListener(e -> {
            try {
                String[] coords = restrictionsInput.getText().split(" ");
                if(coords.length != 4) throw new Exception("Please enter four numbers between 0 and 31 separated by a space.");
                int x1 = Integer.parseInt(coords[0]);
                int y1 = Integer.parseInt(coords[1]);
                int x2 = Integer.parseInt(coords[2]);
                int y2 = Integer.parseInt(coords[3]);
                if(x1 < 0 || x1 > 31 || y1 < 0 || y1 > 31 || x2 < 0 || x2 > 31 || y2 < 0 || y2 > 31) throw new Exception("Numbers must be between 0 and 31 inclusive.");
                if(x1 == x2 && y1 == y2) throw new Exception("Both coordinates cannot be equal.");
                ListNode n1 = new ListNode(x1, y1, emulator.getLevel().getLayerFG().get(new Position(x1, y1)));
                ListNode n2 = new ListNode(x2, y2, emulator.getLevel().getLayerFG().get(new Position(x2, y2)));
                if(!nodesExist(n1, n2)) throw new Exception("Coordinates must refer to nodes.");
                RestrictionNode node = new RestrictionNode(n1, n2);
                if(checkDuplicate(restrictionNodes, node)) throw new Exception("No duplicates allowed.");
                restrictionNodes.addElement(node);
                restrictionsInput.setText("");
            } catch(Exception ex) {
                emulator.throwError(ex.getMessage());
            }
        });

        removeRestrictionButton.addActionListener(e -> {
            int index = restrictionsList.getSelectedIndex();
            if(index != -1) {
                restrictionNodes.remove(index);
            }
        });


        startTempInput.setText("100");
        endTempInput.setText("0.1");
        coolingInput.setText("0.995");
        iterationsInput.setText("20000");

        quickButton.addActionListener(e -> {
            startTempInput.setText("100");
            endTempInput.setText("0.1");
            coolingInput.setText("0.98");
            iterationsInput.setText("10000");
        });

        normalButton.addActionListener(e -> {
            startTempInput.setText("100");
            endTempInput.setText("0.1");
            coolingInput.setText("0.995");
            iterationsInput.setText("20000");
        });

        longButton.addActionListener(e -> {
            startTempInput.setText("100");
            endTempInput.setText("0.05");
            coolingInput.setText("0.998");
            iterationsInput.setText("50000");
        });

        thoroughButton.addActionListener(e -> {
            startTempInput.setText("100");
            endTempInput.setText("0.05");
            coolingInput.setText("0.999");
            iterationsInput.setText("100000");
        });

    }

    private void manageNodeRemoval(ListNode node) {
        for(int i = restrictionNodes.getSize() - 1; i >= 0; i--) {
            RestrictionNode rn = restrictionNodes.getElementAt(i);
            if(rn.before.equals(node) || rn.after.equals(node)) {
                restrictionNodes.remove(i);
            }
        }
    }

    private boolean nodesExist(ListNode n1, ListNode n2) {
        boolean n1Found = false;
        boolean n2Found = false;
        for(int i = 0; i < nodes.getSize(); i++) {
            ListNode n = nodes.getElementAt(i);
            if(n1.equals(n)) {
                n1Found = true;
            }
            if(n2.equals(n)) {
                n2Found = true;
            }
        }
        return n1Found && n2Found;
    }

    private boolean checkDuplicate(ListModel list, Object node) {
        for(int i = 0; i < list.getSize(); i++) {
            Object n = list.getElementAt(i);
            if(n.equals(node)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRunning() {
        return running;
    }

    public class ListNode {
        public int index;
        public Tile t;

        public ListNode(int x, int y, Tile t) {
            this.index = y * 32 + x;
            this.t = t;
        }

        public ListNode(int i, Tile t) {
            this.index = i;
            this.t = t;
        }

        @Override
        public String toString() {
            return "(" + (index % 32) + ", " + (index / 32) + ") - " + t.toString();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }

            if(!(o instanceof ListNode)) {
                return false;
            }

            ListNode node = (ListNode)o;

            return node.index == this.index && node.t == this.t;
        }
    }

    public class RestrictionNode {
        public ListNode before;
        public ListNode after;

        public int beforeIndex;
        public int afterIndex;

        public RestrictionNode(ListNode before, ListNode after) {
            this.before = before;
            this.after = after;
        }

        @Override
        public String toString() {
            return before.toString() + " BEFORE " + after.toString();
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) {
                return true;
            }

            if(!(o instanceof RestrictionNode)) {
                return false;
            }

            RestrictionNode node = (RestrictionNode) o;

            return node.before.equals(this.before) && node.after.equals(this.after);
        }
    }

    private class TSPSolverThread extends Thread {
        private TSPGUI gui;

        TSPSolverThread(TSPGUI gui) {
            this.gui = gui;
        }

        public void run() {
            ArrayList<ListNode> nodesArray = new ArrayList<>();
            ArrayList<ListNode> exitNodesArray = new ArrayList<>();
            ArrayList<RestrictionNode> restrictionNodesArray = new ArrayList<>();

            for(int i = 0; i < nodes.getSize(); i++) {
                nodesArray.add(nodes.getElementAt(i));
            }
            for(int i = 0; i < exitNodes.getSize(); i++) {
                exitNodesArray.add(exitNodes.getElementAt(i));
            }
            for(int i = 0; i < restrictionNodes.getSize(); i++) {
                restrictionNodesArray.add(restrictionNodes.getElementAt(i));
            }

            double startTemp, endTemp, cooling;
            int iterations;

            try {
                startTemp = Double.parseDouble(startTempInput.getText());
                endTemp = Double.parseDouble(endTempInput.getText());
                cooling = Double.parseDouble(coolingInput.getText());
                iterations = Integer.parseInt(iterationsInput.getText());

                if (startTemp <= 0 || endTemp <= 0 || cooling <= 0 || iterations <= 0)
                    throw new Exception("Parameters must be greater than 0.");
                if (startTemp <= endTemp)
                    throw new Exception("Starting temperature must be greater than ending temperature.");
                if (cooling >= 1) throw new Exception("Cooling factor must be less than 1.");
                if(exitNodesArray.size() == 0) throw new Exception("There must be at least one exit tile.");
            } catch(Exception e) {
                emulator.throwError(e.getMessage());
                return;
            }

            try {
                running = true;
                runButton.setText("Stop");
                TSPSolver solver = new TSPSolver(emulator, gui, nodesArray, exitNodesArray, restrictionNodesArray,
                        startTemp, endTemp, cooling, iterations, waterCheckBox.isSelected(), fireCheckBox.isSelected(),
                        bombsCheckBox.isSelected(), thievesCheckBox.isSelected(), trapsCheckBox.isSelected(),
                        output);
                solver.solve();
            } catch(Exception e) {
                output.setText("An error occured:\n  " + e.toString());
                e.printStackTrace();
            } finally {
                runButton.setText("Run");
                killFlag = false;
                running = false;
            }
        }
    }
}
