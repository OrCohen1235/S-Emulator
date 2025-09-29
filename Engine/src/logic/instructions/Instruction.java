package logic.instructions;

import logic.function.Function;
import program.Program;
import logic.label.Label;
import logic.variable.Variable;

public abstract class Instruction {
    private Program program;
    private final InstructionData instructionData;
    private Variable var;
    private final Label label;
    private int degree;
    private Instruction father = null;
    private int indexFatherLocation;
    private static final java.util.concurrent.atomic.AtomicLong SEQ = new java.util.concurrent.atomic.AtomicLong(1);
    private final long uid = SEQ.getAndIncrement();

    public long uid() { return uid; }

    public Instruction(Program program, InstructionData instructionData, Variable var,Label label) {
        this.program = program;
        this.instructionData = instructionData;
        this.var = var;
        this.label = label;
    }

    public Instruction(Program program, InstructionData instructionData ,Label label) {
        this.program = program;
        this.instructionData = instructionData;
        this.label = label;
    }

    public abstract int calcCycles();

    public abstract Label calculateInstruction();

    public abstract String getCommand();

    public String getName() {
        return instructionData.getName();
    }

    public Variable getVar() {
        return var;
    }

    public int getDegree() {
        return degree;
    }

    public Instruction getFather() {
        return father;
    }

    public void setFather(Instruction father) {
        this.father = father;
    }

    public void setProgram(Program program) {
        this.program=program;
    }

    public int getIndexFatherLocation() {
        return indexFatherLocation;
    }

    public void setIndexFatherLocation(int indexFatherLocation) {
        this.indexFatherLocation = indexFatherLocation;
    }

    public Label getLabel() {
        return this.label;
    }

    public int getCycles() {
        return instructionData.getCycles();
    }

    public Long getVarValueFromMap() {
        return switch (var.getType()) {
            case INPUT -> program.getXVariablesFromMap(this.var);
            case WORK -> program.getZVariablesFromMap(this.var);
            case RESULT -> program.getY();
        };
    }

    public Long getVariableValueFromMap(Variable variable) {
        return switch (variable.getType()) {
            case INPUT -> program.getXVariablesFromMap(variable);
            case WORK -> program.getZVariablesFromMap(variable);
            case RESULT -> program.getY();
        };
    }

    public void setVarValueInMap(long newValToSet) {
          switch (var.getType()) {
              case INPUT     -> this.program.setXVariablesToMap(this.var,newValToSet);
              case WORK     -> this.program.setZVariablesToMap(this.var,newValToSet);
              case RESULT     -> this.program.setY(newValToSet);
        };
    }
    public Variable getVarFromMapByString(String valName) {
        return program.getKeyFromMapsByString(valName);
    }

    public Long getValueFromMapByString(String valName) {
        return program.getValueFromMapsByString(valName);
    }

    public Program getProgram() {
        return program;
    }

    public InstructionData getInstructionData() {
        return instructionData;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    @Override
    public final boolean equals(Object o) {
        return (this == o) || (o instanceof Instruction other && this.uid == other.uid);
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(uid);
    }

    public Function getFunctionByName(String name) {
        return program.getFunctionByName(name);
    }

    public Program getInstruncionProgram() {
        return program;
    }
}
