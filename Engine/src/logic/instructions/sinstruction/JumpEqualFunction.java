package logic.instructions.sinstruction;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.JumpInstruction;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import program.Program;

public class JumpEqualFunction extends Instruction implements SyntheticInstruction, JumpInstruction {

    private final Label JEFunctionLabel;
    private Quote funcToQuote;
    private String funcName;
    private String funcArguments;


    public JumpEqualFunction(Program program, Variable var, Label label,String funcName,String funcArguments, Label JEFunctionLabel) {
        super(program, InstructionData.JUMP_EQUAL_FUNCTION, var, label);
        funcToQuote=new Quote(program,var,label,funcName,funcArguments);
        this.funcName=funcName;
        this.funcArguments=funcArguments;
        this.JEFunctionLabel=JEFunctionLabel;
    }

    @Override
    public int calcCycles() {
        return 1+ funcToQuote.calcCycles();
    }

    @Override
    public Label calculateInstruction() {
        long yFromQuote = funcToQuote.calcQuotationValue();
        long varValue = super.getVarValueFromMap();
        if (yFromQuote == varValue) {
            return this.JEFunctionLabel;
        }
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        return "IF" + getVar().getRepresentation() + " = " + "(" + funcName + ", " + funcArguments + ")"
        +"GOTO" + JEFunctionLabel.getLabelRepresentation();
    }

    @Override
    public Label getJumpLabel() {
        return JEFunctionLabel;
    }

    public String getFuncName() {
        return funcName;
    }

    public String getFuncArguments() {
        return funcArguments;
    }
}
