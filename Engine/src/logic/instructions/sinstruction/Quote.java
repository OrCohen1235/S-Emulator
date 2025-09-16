package logic.instructions.sinstruction;

import logic.function.Function;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import program.Program;

import java.util.*;

public class Quote extends Instruction implements SyntheticInstruction {
    private String functionName;
    private String functionArguments;
    private final Function function;


    public Quote(Program program, Variable var, Label label,String queteName, String arguments) {
        super(program, InstructionData.QUOTE, var, label);
        this.functionName = queteName;
        this.functionArguments = arguments;
        function = program.getFunctionByName(functionName);

    }

    private void getVariablesFromMain(){
        Function func = (Function)function;
        for (String val : functionArguments.split(",")) {
            Variable keyVal = getVarFromMapByString(val);
            Long valLong = getValueFromMapByString(val);
            switch (val.charAt(0)) {
                case 'x':
                    func.setXVariablesToMap(keyVal,valLong);
                    break;
                case 'y':
                    func.setY(valLong);
                    break;
                case 'z':
                    func.setZVariablesToMap(keyVal,valLong);
                    break;
            }
        }
    }

    public long calcQuotationValue() {
        return function.getProgramExecutor().run();
    }

    @Override
    public int calcCycles() {
        return 5+function.getProgramExecutor().getSumOfCycles();
    }

    @Override
    public Label calculateInstruction() {
        getVariablesFromMain();
         super.setVarValueInMap(function.getProgramExecutor().run());
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        String returnVal = super.getVar().getRepresentation() + " <- " + "("+ functionName +", ";
        returnVal+= functionArguments +")";
        return returnVal;
    }

    public String getFunctionArguments() {
        return functionArguments;
    }

    public Variable getVarKeyFromMapByString(String val) {
        return function.getKeyFromMapsByString(val);
    }

    public List<Instruction> getInstructionsFromFunction(){
        return function.getInstructionList();
    }
}
