package tools.variation;

import game.Level;
import util.ByteList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class VariationManager {
    private ArrayList<Stmt.Sequence> sequences = new ArrayList<>();
    private HashMap<String, Object> variables;
    private ArrayList<HashMap<String, Object>> variableStates;
    private Level level;
    private Interpreter interpreter;
    private byte[][] permutations;
    public int[] sequenceIndex;
    public byte[][] saveStates;
    public ByteList[] moveLists;
    public boolean finished = false;

    VariationManager(ArrayList<Stmt> statements, HashMap<String, Object> variables, Level level, Interpreter interpreter) {
        setSequences(statements);
        this.variables = variables;
        this.variableStates = new ArrayList<>(sequences.size());
        this.level = level;
        this.interpreter = interpreter;
        this.permutations = new byte[sequences.size()][];
        this.sequenceIndex = new int[sequences.size()];
        this.saveStates = new byte[sequences.size()][];
        this.moveLists = new ByteList[sequences.size()];

        for(int i = 0; i < sequences.size(); i++) {
            permutations[i] = sequences.get(i).permutation.getPermutation();
            variableStates.add(new HashMap<>());
        }

        byte[] initialState = level.save();
        this.saveStates[0] = Arrays.copyOf(initialState, initialState.length);
        this.moveLists[0] = new ByteList();
        setSequenceIndex(statements);
    }

    public byte[] getPermutation(int index) {
        return permutations[index];
    }

    public int nextPermutation() {
        int i;
        for(i = sequences.size() - 1; i >= 0; i--) {
            Stmt.Sequence seq = sequences.get(i);
            seq.permutation.nextPermutation();
            if(seq.permutation.finished) {
                if(i > 0) {
                    seq.permutation.reset();
                    permutations[i] = seq.permutation.getPermutation();
                }
            }
            else  {
                permutations[i] = seq.permutation.getPermutation();
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
}
