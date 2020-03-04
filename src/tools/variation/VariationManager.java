package tools.variation;

import emulator.SuperCC;
import game.Level;
import util.ByteList;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VariationManager {
    private ArrayList<Stmt.Sequence> sequences = new ArrayList<>();
    private HashMap<String, Object> variables;
    private ArrayList<HashMap<String, Object>> variableStates;
    private Level level;
    private Interpreter interpreter;
    public int[] sequenceIndex;
    public byte[][] saveStates;
    public ByteList[] moveLists;
    public boolean finished = false;

    VariationManager(SuperCC emulator, ArrayList<Stmt> statements, HashMap<String, Object> variables,
                     Level level, Interpreter interpreter) {
        setSequences(statements);
        this.variables = variables;
        this.variableStates = new ArrayList<>(sequences.size());
        this.level = level;
        this.interpreter = interpreter;
        if(sequences.size() == 0) {
            return;
        }
        this.sequenceIndex = new int[sequences.size()];
        this.saveStates = new byte[sequences.size()][];
        this.moveLists = new ByteList[sequences.size()];

        for(int i = 0; i < sequences.size(); i++) {
            variableStates.add(new HashMap<>());
        }

        byte[] initialState = level.save();
        this.saveStates[0] = Arrays.copyOf(initialState, initialState.length);
        this.moveLists[0] = new ByteList();

        byte[] moves = emulator.getSavestates().getMoves();
        int index = emulator.getSavestates().getPlaybackIndex();
        if(emulator.getSavestates().getMoveList().size() == 0) {
            index = 0;
        }
        for(int i = 0; i < index; i++) {
            byte move = lowercase(moves[i]);
            moveLists[0].add(move);
        }
        setSequenceIndex(statements);
    }

    public ByteList[] getPermutation(int index) {
        return sequences.get(index).permutation.getPermutation();
    }

    public int nextPermutation() {
        int i;
        for(i = sequences.size() - 1; i >= 0; i--) {
            Stmt.Sequence seq = sequences.get(i);
            seq.permutation.nextPermutation();
            if(seq.permutation.finished) {
                if(i > 0) {
                    seq.permutation.reset();
                }
            }
            else  {
                break;
            }
        }
        if(i == -1) {
            return -1;
        }
        loadVariables(i);
        return i;
    }

    public void setVariables(int index) {
        HashMap<String, Object> newVariables = new HashMap<>();
        for(String var : variables.keySet()) {
            Object newVal = variables.get(var);
            if(newVal instanceof Move) {
                newVal = new Move(((Move) newVal).value);
            }
            newVariables.put(var, newVal);
        }
        variableStates.set(index, newVariables);
        byte[] newSaveState = level.save();
        saveStates[index] = Arrays.copyOf(newSaveState, newSaveState.length);
        moveLists[index] = interpreter.moveList.clone();
    }

    public void terminate(int index) {
        int sum = 0;
        int prevSum = 0;
        for(int i = 0; i < getSequenceCount(); i++) {
            sum += getPermutation(i).length;
            if(index < sum) {
                sequences.get(i).permutation.terminate(index - prevSum);
                for(int j = i + 1; j < getSequenceCount(); j++) {
                    sequences.get(j).permutation.end();
                }
                return;
            }
            prevSum = sum;
        }
    }

    public void terminateZero(int index) {
        sequences.get(index).permutation.reset();
        for(int i = index + 1; i < getSequenceCount(); i++) {
            sequences.get(i).permutation.end();
        }
    }

    private void setSequences(ArrayList<Stmt> statements) {
        for(Stmt stmt : statements) {
            if(stmt instanceof Stmt.Sequence) {
                sequences.add((Stmt.Sequence)stmt);
            }
        }
    }

    private void setSequenceIndex(ArrayList<Stmt> statements) {
        int j = 0;
        for(int i = 0; i < statements.size(); i++) {
            Stmt stmt = statements.get(i);
            if(stmt instanceof Stmt.Sequence) {
                sequenceIndex[j++] = i;
            }
        }
    }

    private void loadVariables(int index) {
        HashMap<String, Object> newVars = variableStates.get(index);
        for(String var : newVars.keySet()) {
            Object val = newVars.get(var);
            if(val instanceof Move) {
                val = new Move(((Move)val).value);
            }
            variables.put(var, val);
        }
    }

    public int getSequenceCount() {
        return sequences.size();
    }

    public double getPermutationCount() {
        double total = 1;
        for(Stmt.Sequence seq : sequences) {
            total *= seq.permutation.getPermutationCount();
        }
        return total;
    }

    public boolean validate() {
        if(getSequenceCount() == 0) {
            interpreter.print("Script must contain at least 1 sequence\n", new Color(255, 68, 68));
            return false;
        }
        for(Stmt.Sequence sequence : sequences) {
            if(sequence.permutation.limits.upper == 0) {
                interpreter.print("Sequence upper bound must be at least 1\n", new Color(255, 68, 68));
                return false;
            }
        }
        return true;
    }

    public void printPermutations() {
        int count = 0;
        int[] subset = sequences.get(0).permutation.getSubset();
        do {
            for(int i = 0; i < getSequenceCount(); i++) {
                Stmt.Sequence seq = sequences.get(i);
                for(ByteList list : seq.permutation.getPermutation()) {
                    for(byte b : list) {
                        System.out.print((char)b);
                    }
                }
                count++;
                System.out.print(" | [");
                for(int s : subset) {
                    System.out.print(s + ", ");
                }
                System.out.print("]");
            }
            System.out.println();
        }while(nextPermutation() != -1);
        System.out.println("Count: " + count);
    }

    private byte lowercase(byte b) {
        switch(b) {
            case 'U':
                return 'u';
            case 'R':
                return 'r';
            case 'D':
                return 'd';
            case 'L':
                return 'l';
            default:
                return b;
        }
    }
}
