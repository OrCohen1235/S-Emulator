package logic.execution;

import logic.instructions.Instruction;
import logic.instructions.sinstruction.Quote;
import program.Program;
import logic.label.*;
import logic.label.Label;
import users.UserManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class ProgramExecutorImpl {

    private final Program program; // Reference to the program being executed
    private int sumOfCycles;
    private int currentIndex=0;
    private Boolean isFinishDebugging=false;
    private long stoppedResult=0;

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

    public long getStoppedResult() {
        return stoppedResult;
    }

    public long run(int currentCredits) throws Exception {
        int index = 0; // Start from the first instruction
        Label nextLabel;

        do {
            Instruction currentInstruction = program.getActiveInstruction(index);

            nextLabel = currentInstruction.calculateInstruction();
            int currentCyclesInstruction = currentInstruction.getCycles();// Execute and get next label
            sumOfCycles += currentCyclesInstruction;  // Add cycles of current instruction
            if (currentCredits!= -1){
                if (currentInstruction.getCycles() > currentCredits) {
                    stoppedResult = program.getY();
                    throw new Exception("Not enough credits!");
                }
                currentCredits -= currentCyclesInstruction;
            }

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

    public long runDebugger(int level,int currentCredits) throws Exception {
        int index = currentIndex;
        Label nextLabel;
        if (level == -1)
        {
            isFinishDebugging=true;
        }

        do {
            Instruction currentInstruction = program.getActiveInstruction(index);

            nextLabel = currentInstruction.calculateInstruction();
            int currentCyclesInstruction = currentInstruction.getCycles();// Execute and get next label
            sumOfCycles += currentCyclesInstruction;  // Add cycles of current instruction
            if (currentCredits!= -1){
                if (currentInstruction.getCycles() > currentCredits) {
                    stoppedResult = program.getY();
                    throw new Exception("Not enough credits!");
                }
                currentCredits -= currentCyclesInstruction;
            }

            if (nextLabel == FixedLabel.EMPTY) {
                index++; // Move to next instruction
            }
            else if (!Objects.equals(nextLabel.getLabelRepresentation(), FixedLabel.EXIT.getLabelRepresentation())) {
                // Jump to instruction at target label
                currentInstruction = program.getInstructionByLabelActive(nextLabel);
                index = program.getIndexByInstruction(currentInstruction);
            }
            else {
                isFinishDebugging = true;
                break;
            }// Exit condition
        } while (isFinishDebugging && nextLabel != FixedLabel.EXIT && index < program.getSizeOfInstructions());

        this.currentIndex = index;
        if (index >= program.getSizeOfInstructions())
        {
            isFinishDebugging=true;
        }

        return program.getY(); // Return output value after execution
    }


    public int getCurrentIndex() {
        return currentIndex;
    }

    public Boolean getFinishDebugging() {
        return isFinishDebugging;
    }

    public void resetDebugger() {
        isFinishDebugging=false;
        currentIndex=0;
        sumOfCycles=0;
    }



}
