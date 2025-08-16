package Logic.execution;

import Logic.Instruction;
import Logic.Program;
import Logic.label.*;

import java.util.Objects;

public class ProgramExecutorImpl{

    private final Program program;

    public ProgramExecutorImpl(Program program) {
        this.program = program;
    }

    public long run(Long... input) {
        int index =0;
        Label nextLabel;
        do {
            Instruction currentInstruction = program.getInstruction(index);
            nextLabel = currentInstruction.calculateInstruction();

            if (nextLabel == FixedLabel.EMPTY) {
                index++;
            }
            else if (!Objects.equals(nextLabel.getLabelRepresentation(), FixedLabel.EXIT.getLabelRepresentation())) {
                currentInstruction = program.getInstructionByLabel(nextLabel);
                index = program.getIndexInstruction(currentInstruction);
            }
            else
                break;
        } while (nextLabel != FixedLabel.EXIT);

        return program.getY();
    }

}
