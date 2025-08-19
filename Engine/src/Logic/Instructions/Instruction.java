package Logic.Instructions;

import Logic.Program;
import Logic.label.Label;
import Logic.variable.Variable;

public abstract class Instruction {
    private final Program program;
    private final InstructionData instructionData;
    private Variable var;
    private final Label label;
    private int degree;
    private Instruction father=null;

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

    public Label getLabel() {
        return this.label;
    }

    public int getCycles() {
        return instructionData.getCycles();
    }

    public Long getVarValueFromMap() {
        return switch (var.getType()) {
            case INPUT -> program.getXVirablesFromMap(this.var);
            case WORK -> program.getZVirablesFromMap(this.var);
            case RESULT -> program.getY();
        };
    }

    public Long getVariableValueFromMap(Variable variable) {
        return switch (variable.getType()) {
            case INPUT -> program.getXVirablesFromMap(variable);
            case WORK -> program.getZVirablesFromMap(variable);
            case RESULT -> program.getY();
        };
    }

    public void setVarValueInMap(long newValToSet) {
          switch (var.getType()) {
              case INPUT     -> this.program.setxVirablesToMap(this.var,newValToSet);
              case WORK     -> this.program.setzVirablesToMap(this.var,newValToSet);
              case RESULT     -> this.program.setY(newValToSet);
        };
    }

    public InstructionData getInstructionData() {
        return instructionData;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }


}
