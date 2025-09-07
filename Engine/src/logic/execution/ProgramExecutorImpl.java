package logic.execution;

import logic.instructions.Instruction;
import program.Program;
import logic.label.*;
import logic.label.Label;

import java.util.Objects;

public class ProgramExecutorImpl {

    private final Program program; // Reference to the program being executed
    private int sumOfCycles;
    private Instruction currentInstruction;// Tracks total cycles executed

    public ProgramExecutorImpl(Program program) {
        this.program = program;
        this.sumOfCycles = 0; // Initialize cycle counter
    }

    public int getSumOfCycles() {
        return sumOfCycles; // Return total cycles executed
    }

    public void resetSumOfCycles() {
        this.sumOfCycles = 0; // Reset cycle counter
    }

    public long run() {
        int index = 0; // Start from the first instruction
        Label nextLabel;

        do {
            Instruction currentInstruction = program.getActiveInstruction(index);
            sumOfCycles += currentInstruction.getCycles(); // Add cycles of current instruction
            nextLabel = currentInstruction.calculateInstruction(); // Execute and get next label

            if (nextLabel == FixedLabel.EMPTY) {
                index++; // Move to next instruction
            }
            else if (!Objects.equals(nextLabel.getLabelRepresentation(), FixedLabel.EXIT.getLabelRepresentation())) {
                // Jump to instruction at target label
                currentInstruction = program.getInstructionByLabelActive(nextLabel);
                index = program.getIndexByInstruction(currentInstruction);
            }
            else
                break; // Exit condition
        } while (nextLabel != FixedLabel.EXIT && index < program.getSizeOfInstructions());

        return program.getY(); // Return output value after execution
    }

    public long runDebugger(int level) {
        int index = 0; // Start from the first instruction
        Label nextLabel;

        do {
            level--;
            Instruction currentInstruction = program.getActiveInstruction(index);
            this.currentInstruction =currentInstruction;
            sumOfCycles += currentInstruction.getCycles(); // Add cycles of current instruction
            nextLabel = currentInstruction.calculateInstruction(); // Execute and get next label

            if (nextLabel == FixedLabel.EMPTY) {
                index++; // Move to next instruction
            }
            else if (!Objects.equals(nextLabel.getLabelRepresentation(), FixedLabel.EXIT.getLabelRepresentation())) {
                // Jump to instruction at target label
                currentInstruction = program.getInstructionByLabelActive(nextLabel);
                index = program.getIndexByInstruction(currentInstruction);
            }
            else
                break; // Exit condition
        } while (nextLabel != FixedLabel.EXIT && index < program.getSizeOfInstructions() && level!=0);

        return program.getY(); // Return output value after execution
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }
}
