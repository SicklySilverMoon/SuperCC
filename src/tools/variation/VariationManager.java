package tools.variation;

import emulator.SuperCC;
import game.Level;
import util.CharList;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class VariationManager {
    private ArrayList<Stmt.Sequence> sequences = new ArrayList<>();
    private HashMap<String, Object> variables;
    private ArrayList<HashMap<String, Object>> variableStates;
    private Level level;
    private Interpreter interpreter;
    public int[] sequenceIndex;
    public byte[][] saveStates;
    public CharList[] moveLists;
    private ArrayList<Double> cumulativeTotalPermutations = new ArrayList<>();
    private double totalValidPermutationCount = 1;
    private int lastIndex = -1;

    VariationManager(SuperCC emulator, ArrayList<Stmt> statements, HashMap<String, Object> variables,
                     Level level, Interpreter interpreter) {
        setSequences(statements);
        this.variables = variables;
        this.variableStates = new ArrayList<>(sequences.size());
        this.level = level;
        this.interpreter = interpreter;
        calculatePermutationCount();
        if(sequences.size() == 0) {
            return;
        }
        this.sequenceIndex = new int[sequences.size()];
        this.saveStates = new byte[sequences.size()][];
        this.moveLists = new CharList[sequences.size()];

        for(int i = 0; i < sequences.size(); i++) {
            variableStates.add(new HashMap<>());
        }

        byte[] initialState = level.save();
        this.saveStates[0] = Arrays.copyOf(initialState, initialState.length);
        this.moveLists[0] = new CharList();

        char[] moves = emulator.getSavestates().getMoves();
        int index = emulator.getSavestates().getPlaybackIndex();
        if(emulator.getSavestates().getMoveList().size() == 0) {
            index = 0;
        }
        for(int i = 0; i < index; i++) {
            char move = lowercase(moves[i]);
            moveLists[0].add(move);
        }
        setSequenceIndex(statements);
    }

    public CharList[] getPermutation(int index) {
        return sequences.get(index).permutation.getPermutation();
    }

    public int nextPermutation() {
        for(int i = sequences.size() - 1; i >= 0; i--) {
            Stmt.Sequence seq = sequences.get(i);
            seq.permutation.nextPermutation();
            if(seq.permutation.finished) {
                seq.permutation.reset();
            }
            else  {
                loadVariables(i);
                return i;
            }
        }
        return -1;
    }

    public void setVariables(int index) {
        if(index != lastIndex) {
            HashMap<String, Object> newVariables = new HashMap<>();
            for (String var : variables.keySet()) {
                Object newVal = variables.get(var);
                if (newVal instanceof Move) {
                    newVal = new Move(((Move) newVal).value);
                }
                newVariables.put(var, newVal);
            }
            variableStates.set(index, newVariables);
            byte[] newSavestate = level.save();
            saveStates[index] = Arrays.copyOf(newSavestate, newSavestate.length);
            moveLists[index] = interpreter.moveList.clone();
            lastIndex = index;
        }
    }

    public void terminate(int index) {
        int sum = 0;
        int prevSum = 0;
        for(int i = 0; i < getSequenceCount(); i++) {
            sum += getPermutation(i).length;
            if(index < sum) {
                sequences.get(i).permutation.terminate(index - prevSum);
                endSequences(i + 1);
                return;
            }
            prevSum = sum;
        }
    }

    public void terminateZero(int index) {
        sequences.get(index).permutation.reset();
        endSequences(index + 1);
    }

    private void endSequences(int from) {
        for(int i = from; i < getSequenceCount(); i++) {
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

    public void calculatePermutationCount() {
        double total = 1;
        cumulativeTotalPermutations.add(total);
        for(int i = sequences.size() - 1; i >= 0; i--) {
            total *= sequences.get(i).permutation.permutationCount;
            cumulativeTotalPermutations.add(total);
            totalValidPermutationCount *= sequences.get(i).permutation.permutationValidCount;
        }
        Collections.reverse(cumulativeTotalPermutations);
    }

    public double getPermutationIndex() {
        int index = 0;
        for(int i = 0; i < sequences.size(); i++) {
            index += sequences.get(i).permutation.getPermutationIndex() * cumulativeTotalPermutations.get(i + 1);
        }
        return index;
    }

    public double getTotalPermutationCount() {
        return cumulativeTotalPermutations.get(0);
    }

    public double getTotalValidPermutationCount() {
        return totalValidPermutationCount;
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

    private char lowercase(char b) {
        switch(b) {
            case 'U':
                return SuperCC.UP;
            case 'R':
                return SuperCC.RIGHT;
            case 'D':
                return SuperCC.DOWN;
            case 'L':
                return SuperCC.LEFT;
            default:
                return b;
        }
    }
}
