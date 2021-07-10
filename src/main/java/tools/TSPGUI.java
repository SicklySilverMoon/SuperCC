package tools;

import emulator.SuperCC;
import game.Position;
import game.Tile;
import tools.tsp.ActingWallParameters;
import tools.tsp.SimulatedAnnealingParameters;
import tools.tsp.TSPSolver;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TSPGUI {
    private JPanel mainPanel;
    private JList<ListNode> nodesList;
    private JList<ListNode> exitsList;
    private JList<RestrictionNode> restrictionsList;
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
    private boolean hasGui = true;

    public boolean killFlag = false;
    public static boolean running = false;

    public TSPGUI(SuperCC emulator) {
        this.emulator = emulator;
        setup();
    }

    public TSPGUI(SuperCC emulator, boolean hasGui) {
        this.emulator = emulator;
        this.hasGui = hasGui;
        setup();
    }

    public ArrayList<ListNode> getAllChips() {
        addAllTilesToContainer(nodes, Tile.CHIP);
        return listModelToArrayList(nodes);
    }

    public ArrayList<ListNode> getAllExits() {
        addAllTilesToContainer(exitNodes, Tile.EXIT);
        return listModelToArrayList(exitNodes);
    }

    private void setup() {
        if (nodesList == null || exitsList == null || restrictionsList == null) {
            nodesList = new JList<>(); //yes yes its overriden by the IntelliJ forms generator, but maven doesn't
            exitsList = new JList<>(); //have that so this prevents errors from building it with maven
            restrictionsList = new JList<>();
        }

        this.nodesList.setModel(nodes);
        this.exitsList.setModel(exitNodes);
        this.restrictionsList.setModel(restrictionNodes);

        if(hasGui) {
            JFrame frame = new JFrame("TSP Solver");
            frame.setContentPane(mainPanel);
            frame.pack();
            frame.setVisible(true);
            frame.addWindowListener(new WindowListener() {
                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    killFlag = true;
                }

                //Unused, but part of interface
                @Override
                public void windowOpened(WindowEvent windowEvent) {
                }

                @Override
                public void windowClosed(WindowEvent windowEvent) {
                }

                @Override
                public void windowIconified(WindowEvent windowEvent) {
                }

                @Override
                public void windowDeiconified(WindowEvent windowEvent) {
                }

                @Override
                public void windowActivated(WindowEvent windowEvent) {
                }

                @Override
                public void windowDeactivated(WindowEvent windowEvent) {
                }
            });

            runButton.addActionListener(e -> {
                if (running) {
                    killFlag = true;
                    return;
                }
                new TSPSolverThread(this).start();
            });


            addNodeButton.addActionListener(e -> {
                try {
                    String[] coords = nodesInput.getText().split(" ");
                    if (coords.length != 2)
                        throw new Exception("Please enter two numbers between 0 and 31 separated by a space.");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    if (x < 0 || x > 31 || y < 0 || y > 31)
                        throw new Exception("Numbers must be between 0 and 31 inclusive.");

                    ListNode node = new ListNode(x, y, emulator.getLevel().getLayerFG().get(new Position(x, y)));
                    if (checkDuplicate(nodes, node))
                        throw new Exception("No duplicates allowed");
                    nodes.addElement(node);
                    nodesInput.setText("");
                } catch (Exception ex) {
                    emulator.throwError(ex.getMessage());
                }
            });

            removeNodeButton.addActionListener(e -> {
                int index = nodesList.getSelectedIndex();
                if (index != -1) {
                    removeRelatedRestrictions(nodes.getElementAt(index));
                    nodes.remove(index);
                }
            });

            allChipsButton.addActionListener(e -> {
                nodes.clear();
                restrictionNodes.clear();
                addAllTilesToContainer(nodes, Tile.CHIP);
            });


            addExitButton.addActionListener(e -> {
                try {
                    String[] coords = exitsInput.getText().split(" ");
                    if (coords.length != 2)
                        throw new Exception("Please enter two numbers between 0 and 31 separated by a space.");
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    if (x < 0 || x > 31 || y < 0 || y > 31)
                        throw new Exception("Numbers must be between 0 and 31 inclusive.");

                    ListNode node = new ListNode(x, y, emulator.getLevel().getLayerFG().get(new Position(x, y)));
                    if (checkDuplicate(exitNodes, node))
                        throw new Exception("No duplicates allowed.");
                    exitNodes.addElement(node);
                    exitsInput.setText("");
                } catch (Exception ex) {
                    emulator.throwError(ex.getMessage());
                }
            });

            removeExitButton.addActionListener(e -> {
                int index = exitsList.getSelectedIndex();
                if (index != -1) {
                    exitNodes.remove(index);
                }
            });

            allExitsButton.addActionListener(e -> {
                exitNodes.clear();
                addAllTilesToContainer(exitNodes, Tile.EXIT);
            });


            addRestrictionButton.addActionListener(e -> {
                try {
                    String[] coords = restrictionsInput.getText().split(" ");
                    if (coords.length != 4)
                        throw new Exception("Please enter four numbers between 0 and 31 separated by a space.");
                    int x1 = Integer.parseInt(coords[0]);
                    int y1 = Integer.parseInt(coords[1]);
                    int x2 = Integer.parseInt(coords[2]);
                    int y2 = Integer.parseInt(coords[3]);
                    if (x1 < 0 || x1 > 31 || y1 < 0 || y1 > 31 || x2 < 0 || x2 > 31 || y2 < 0 || y2 > 31)
                        throw new Exception("Numbers must be between 0 and 31 inclusive.");
                    if (x1 == x2 && y1 == y2)
                        throw new Exception("Both coordinates cannot be equal.");

                    ListNode n1 = new ListNode(x1, y1, emulator.getLevel().getLayerFG().get(new Position(x1, y1)));
                    ListNode n2 = new ListNode(x2, y2, emulator.getLevel().getLayerFG().get(new Position(x2, y2)));
                    if (!nodesExist(n1, n2))
                        throw new Exception("Coordinates must refer to nodes.");

                    RestrictionNode node = new RestrictionNode(n1, n2);
                    if (checkDuplicate(restrictionNodes, node))
                        throw new Exception("No duplicates allowed.");
                    restrictionNodes.addElement(node);
                    restrictionsInput.setText("");
                } catch (Exception ex) {
                    emulator.throwError(ex.getMessage());
                }
            });

            removeRestrictionButton.addActionListener(e -> {
                int index = restrictionsList.getSelectedIndex();
                if (index != -1) {
                    restrictionNodes.remove(index);
                }
            });


            setParameters(100, 0.1, 0.995, 20000);

            quickButton.addActionListener(e -> {
                setParameters(100, 0.1, 0.98, 10000);
            });

            normalButton.addActionListener(e -> {
                setParameters(100, 0.1, 0.995, 20000);
            });

            longButton.addActionListener(e -> {
                setParameters(100, 0.05, 0.998, 50000);
            });

            thoroughButton.addActionListener(e -> {
                setParameters(100, 0.05, 0.999, 100000);
            });
        }
    }

    private void removeRelatedRestrictions(ListNode node) {
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

    private void addAllTilesToContainer(DefaultListModel<ListNode> container, Tile tile) {
        for(int i = 0; i < 1024; i++) {
            Tile foregroundTile = emulator.getLevel().getLayerFG().get(i);
            if(foregroundTile == tile) {
                container.addElement(new ListNode(i, foregroundTile));
            }

            Tile backgroundTile = emulator.getLevel().getLayerBG().get(i);
            if(backgroundTile == tile) {
                container.addElement(new ListNode(i, backgroundTile));
            }
        }
    }

    private void setParameters(double startTemp, double endTemp, double cooling, int iterations) {
        startTempInput.setText(startTemp + "");
        endTempInput.setText(endTemp + "");
        coolingInput.setText(cooling + "");
        iterationsInput.setText(iterations + "");
    }

    private <T> ArrayList<T> listModelToArrayList(DefaultListModel<T> listModel) {
        ArrayList<T> nodeList = new ArrayList<>();
        for(int i = 0; i < listModel.getSize(); i++) {
            nodeList.add(listModel.getElementAt(i));
        }
        return nodeList;
    }

    public static boolean isRunning() {
        return running;
    }

    public static class ListNode {
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ListNode listNode = (ListNode) o;
            return index == listNode.index &&
                    t == listNode.t;
        }

        @Override
        public int hashCode() {
            return Objects.hash(index, t);
        }
    }

    public static class RestrictionNode {
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
            ArrayList<ListNode> nodesArray = listModelToArrayList(nodes);
            ArrayList<ListNode> exitNodesArray = listModelToArrayList(exitNodes);
            ArrayList<RestrictionNode> restrictionNodesArray = listModelToArrayList(restrictionNodes);

            SimulatedAnnealingParameters simulatedAnnealingParameters;
            try {
                double startTemp = Double.parseDouble(startTempInput.getText());
                double endTemp = Double.parseDouble(endTempInput.getText());
                double cooling = Double.parseDouble(coolingInput.getText());
                int iterations = Integer.parseInt(iterationsInput.getText());
                simulatedAnnealingParameters = new SimulatedAnnealingParameters(startTemp, endTemp, cooling, iterations);

                if (startTemp <= 0 || endTemp <= 0 || cooling <= 0 || iterations <= 0) throw new Exception("Parameters must be greater than 0.");
                if (startTemp <= endTemp) throw new Exception("Starting temperature must be greater than ending temperature.");
                if (cooling >= 1) throw new Exception("Cooling factor must be less than 1.");
                if(exitNodesArray.size() == 0) throw new Exception("There must be at least one exit tile.");
            } catch(Exception e) {
                emulator.throwError(e.getMessage());
                return;
            }

            ActingWallParameters actingWallParameters = new ActingWallParameters(
                    waterCheckBox.isSelected(),
                    fireCheckBox.isSelected(),
                    bombsCheckBox.isSelected(),
                    thievesCheckBox.isSelected(),
                    trapsCheckBox.isSelected()
            );

            running = true;
            runButton.setText("Stop");
            TSPSolver solver = new TSPSolver(emulator, gui, nodesArray, exitNodesArray, restrictionNodesArray,
                    simulatedAnnealingParameters, actingWallParameters, output);

            try {
                solver.solve();
            } catch(Exception e) {
                output.setText("An error occured:\n" + e.getMessage());
            } finally {
                runButton.setText("Run");
                killFlag = false;
                running = false;
            }
        }
    }
}
