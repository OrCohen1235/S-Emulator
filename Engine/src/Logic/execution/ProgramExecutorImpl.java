package Logic.execution;

import Logic.Instruction;
import Logic.Program;
import Logic.label.*;

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
            } else if (nextLabel != FixedLabel.EXIT) {
                currentInstruction = program.getInstractionByLabel(nextLabel);
                index = program.getIndexInstraction(currentInstruction);
            }
        } while (nextLabel != FixedLabel.EXIT);

        return program.getY();
    }

}
