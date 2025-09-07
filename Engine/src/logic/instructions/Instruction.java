package logic.instructions;

import program.Program;
import logic.label.Label;
import logic.variable.Variable;

import java.util.Objects;

public abstract class Instruction {
    private final Program program;
    private final InstructionData instructionData;
    private Variable var;
    private final Label label;
    private int degree;
    private Instruction father = null;
    private int indexFatherLocation;

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

    public InstructionData getInstructionData() {
        return instructionData;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Instruction that)) return false;
        return degree == that.degree && instructionData == that.instructionData && Objects.equals(var, that.var) && Objects.equals(label, that.label) && Objects.equals(father, that.father);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instructionData, var, label, degree, father);
    }
}
