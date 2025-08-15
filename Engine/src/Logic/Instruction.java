package Logic;

import Logic.label.Label;
import Logic.variable.Variable;

import java.util.*;

public abstract class Instruction {
    private Program program;
    private InstructionData instructionData;
    private Variable var;
    private Label label;
    private int degree;

    public Instruction(Program program, InstructionData instructionData, Variable var,Label label) {
        this.program = program;
        this.instructionData = instructionData;
        this.var = var;
        this.label = label;
    }
    public Instruction(){};

    public abstract int getDegree();

    public abstract int calcCycles();

    public abstract Label calculateInstraction();

    public String getName() {
        return instructionData.getName();
    }

    public Variable getVar() {
        return var;
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

    public void setVarValueFromMap(long newValToSet) {
          switch (var.getType()) {
              case INPUT     -> this.program.setxVirablesToMap(this.var,newValToSet);
              case WORK     -> this.program.setzVirablesToMap(this.var,newValToSet);
              case RESULT     -> this.program.setY(newValToSet);
        };
    }

}
