package Logic.execution;

import Logic.Instructions.Instruction;
import Program.Program;
import Logic.label.*;
import Logic.label.Label;

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

    public long run(Long... input) {
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
                index = program.GetIndexByInstruction(currentInstruction);
            }
            else
                break;
        } while (nextLabel != FixedLabel.EXIT);

        return program.getY();
    }

//    public long runByDegree(Long... input) {
//        int index =0;
//        Label nextLabel;
//        do {
//            Instruction currentInstruction = program.getInstructionByDegree(index);
//            sumOfCycles += currentInstruction.getCycles();
//            nextLabel = currentInstruction.calculateInstruction();
//
//            if (nextLabel == FixedLabel.EMPTY) {
//                index++;
//            }
//            else if (!Objects.equals(nextLabel.getLabelRepresentation(), FixedLabel.EXIT.getLabelRepresentation())) {
//                currentInstruction = program.getInstructionByLabelFromDegreeList(nextLabel);
//                index = program.getIndexInstructionByDegree(currentInstruction);
//            }
//            else
//                break;
//        } while (nextLabel != FixedLabel.EXIT);
//
//        return program.getY();
//    }

}
