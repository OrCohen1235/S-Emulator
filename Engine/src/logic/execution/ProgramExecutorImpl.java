package logic.execution;

import logic.instructions.Instruction;
import program.Program;
import logic.label.*;
import logic.label.Label;

import java.util.Objects;

public class ProgramExecutorImpl {

    private final Program program;
    private int sumOfCycles;

    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.sumOfCycles = 0;
    }

    public int getSumOfCycles() {
        return sumOfCycles;
    }

    public void setSumOfCycles(int sumOfCycles) {
        this.sumOfCycles = sumOfCycles;
    }

    public long run() {
        int index =0;
        Label nextLabel;
        do {
            Instruction currentInstruction = program.getActiveInstruction(index);
            sumOfCycles += currentInstruction.getCycles();
            nextLabel = currentInstruction.calculateInstruction();

            if (nextLabel == FixedLabel.EMPTY) {
                index++;
            }
            else if (!Objects.equals(nextLabel.getLabelRepresentation(), FixedLabel.EXIT.getLabelRepresentation())) {
                currentInstruction = program.getInstructionByLabelActive(nextLabel);
                index = program.getIndexByInstruction(currentInstruction);
            }
            else
                break;
        } while (nextLabel != FixedLabel.EXIT && index < program.getSizeOfInstructions());

        return program.getY();
    }
}
