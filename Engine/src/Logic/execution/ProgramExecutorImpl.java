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
            Instruction currentInstruction = program.getInstraction(index);
            nextLabel = currentInstruction.calculateInstraction(); //  L1: y-- /n L2: go to L3 /n l5: x1-- /n L4: goto l1

            if (nextLabel == FixedLabel.EMPTY) {
                index++;
            }
            else if (!Objects.equals(nextLabel.getLabelRepresentation(), FixedLabel.EXIT.getLabelRepresentation())) {
                currentInstruction = program.getInstractionByLabel(nextLabel);
                index = program.getIndexInstraction(currentInstruction);
            }
            else
                break;
        } while (nextLabel != FixedLabel.EXIT);

        return program.getY();
    }

}
