package logic.expansion;

import logic.function.Function;
import logic.instructions.JumpInstruction;
import logic.instructions.binstruction.Decrease;
import logic.instructions.binstruction.Increase;
import logic.instructions.binstruction.JumpNotZero;
import logic.instructions.binstruction.Neutral;
import logic.instructions.Instruction;
import logic.instructions.sinstruction.*;
import logic.label.LabelImpl;
import program.Program;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import program.ProgramLoadException;

import java.util.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;

public class Expander {

    private final ExpansionContext context; // Provides fresh labels/work vars and program context
    private final Program program;          // Target program to build expanded instructions for

    public Expander(ExpansionContext ctx) {
        this.context = ctx;
        this.program = context.getProgram();
    }

    public List<Instruction> expand(Instruction inst) {
        return switch (inst.getInstructionData()) {
            case ASSIGNMENT -> expandAssignment((Assignment) inst);                      // v <- v1
            case CONSTANT_ASSIGNMENT ->  expandConstantAssignment((ConstantAssignment) inst); // v <- K
            case GOTO_LABEL ->  expandGotoLabel((GotoLabel) inst);                       // unconditional jump
            case JUMP_EQUAL_CONSTANT  -> expandJumpEqualsConstant((JumpEqualConstant) inst); // if v == K goto L
            case JUMP_EQUAL_VARIABLE ->  expandJumpEqualsVariable((JumpEqualVariable) inst); // if v == v' goto L
            case JUMP_ZERO ->  expandJumpZero((JumpZero) inst);                          // if v == 0 goto L
            case ZERO_VARIABLE ->   expandZeroVariable((ZeroVariable) inst);             // v <- 0
            case JUMP_NOT_ZERO, INCREASE, DECREASE, NEUTRAL -> List.of(inst);
            case QUOTE -> expandQuote((Quote)inst);// already primitive
            case JUMP_EQUAL_FUNCTION -> expandJumpEqualFunction((JumpEqualFunction) inst) ;
        };
    }

    private List<Instruction> expandJumpEqualFunction(JumpEqualFunction instruction) {
        Variable z1 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel();
        Label instructionLabel = instruction.getLabel();
        Variable v = instruction.getVar();
        Label jumpLabel= instruction.getJumpLabel();


        return List.of(
                new Quote(program,z1, instructionLabel, instruction.getFuncName(),instruction.getFuncArguments()),
                new JumpEqualVariable(program,v,jumpLabel, z1));

    }


    private List<Instruction> expandQuote(Quote quote) {
        List<Instruction> result = new ArrayList<>();

        Map<Variable,Variable> varsQuote= new HashMap<>();
        Map<Label,Label> labelsQuote= new HashMap<>();

        List<String> functionArgumentsList = quote.functionArgumentsToStringList(quote.getFunctionArguments());
        List<Variable> xVars = quote.getFunction().getMainProgram().getInputVariables();
        int i = 0;

        for (String arg : functionArgumentsList) {
            if (quote.isFirstArgIsVar(arg)){
                if (!arg.equals("")) {
                    Variable value = context.getFreshWorkVal();
                    Variable key = quote.getFunction().getMainProgram().getKeyFromMapsByString(arg);
                    varsQuote.put(key, value);
                    Assignment assignment = new Assignment(program, value, key, FixedLabel.EMPTY);
                    result.add(assignment);
                }
            } else {
                Variable value = context.getFreshWorkVal();
                Variable key = xVars.get(i);
                i++;
                varsQuote.put(key, value);

                String nameOffunctionToQuote;
                String functionsArgumentsToQuote = "";
                if (arg.indexOf(",") != -1){
                    nameOffunctionToQuote = arg.substring(0,arg.indexOf(","));
                    functionsArgumentsToQuote = arg.substring(arg.indexOf(",") + 1);
                } else {
                    nameOffunctionToQuote = arg;
                }

                Quote newQuote = new Quote(program, value, FixedLabel.EMPTY, nameOffunctionToQuote, functionsArgumentsToQuote);
                result.add(newQuote);
            }
        }

        List<Instruction> oldInstructions = quote.getInstructionsFromFunction();
        for (Instruction inst : oldInstructions) {
            Variable varToReplace = inst.getVar();
            Variable newVar = varsQuote.get(varToReplace);
            if (newVar == null) {
                newVar = context.getFreshWorkVal();
                varsQuote.put(varToReplace, newVar);
            }

            Label oldLabel = inst.getLabel();
            Label newLabel;
            if (oldLabel.getLabelRepresentation() != "" && !labelsQuote.containsKey(oldLabel)) {
                 newLabel= context.getFreshLabel();
                labelsQuote.put(oldLabel, newLabel);
            }
            else {
                newLabel= new LabelImpl("");
            }

            Label newJumpLabel;
            if (inst instanceof JumpInstruction) {
                JumpInstruction jump = (JumpInstruction) inst;
                Label oldJumpLabel = jump.getJumpLabel();

                if (labelsQuote.containsKey(oldJumpLabel)) {
                    newJumpLabel = labelsQuote.get(oldJumpLabel);
                }
                else {
                    newJumpLabel = context.getFreshLabel();
                    labelsQuote.put(oldJumpLabel, newJumpLabel);
                }
            }
            else {
                newJumpLabel = new LabelImpl("");
            }
            result.add(createInstructionToQuote(newVar,newLabel,newJumpLabel,varsQuote,inst));
        }
        Assignment assignment;
        Label assignmentLabel=new LabelImpl("EMPTY");
        if (labelsQuote.containsKey(FixedLabel.EXIT)){
            assignmentLabel = labelsQuote.get(FixedLabel.EXIT);
        }
        assignment=new Assignment(program,quote.getVar(),varsQuote.get(Variable.RESULT),assignmentLabel);
        result.add(assignment);
        return result;

    }

    public Instruction createInstructionToQuote(Variable newVar,Label newLabel,Label newJumpLabel, Map<Variable,Variable> varsQuote,Instruction inst ) {
        try {
            // Map instruction name to concrete Instruction instance
            return switch (inst.getInstructionData()) {
                case INCREASE -> new Increase(program, newVar, newLabel);
                case DECREASE -> new Decrease(program, newVar, newLabel);
                case NEUTRAL  -> new Neutral(program, newVar, newLabel);

                case JUMP_NOT_ZERO -> {
                    yield new JumpNotZero(program, newVar, newJumpLabel, newLabel);
                }
                case ASSIGNMENT -> {
                    Assignment assignment = (Assignment) inst;
                    Variable assignedVariable = assignment.getAssignedVariable();

                    if (assignedVariable == null) {
                        yield new Assignment(program, newVar, context.getFreshWorkVal(), newLabel);
                    } else {
                        Variable mappedVar = varsQuote.get(assignedVariable);

                        if (mappedVar == null) {
                            mappedVar = context.getFreshWorkVal();
                        }
                        yield new Assignment(program, newVar, mappedVar, newLabel);
                    }
                }
                case CONSTANT_ASSIGNMENT -> {
                    yield new ConstantAssignment(program, newVar, ((ConstantAssignment) inst).getConstantValue(), newLabel);
                }
                case GOTO_LABEL -> {

                    yield new GotoLabel(program, newJumpLabel, newLabel);
                }
                case JUMP_ZERO -> {

                    yield new JumpZero(program, newVar, newJumpLabel, newLabel);
                }
                case ZERO_VARIABLE -> new ZeroVariable(program, newVar, newLabel);
                case JUMP_EQUAL_CONSTANT -> {
                    yield new JumpEqualConstant(program, newVar, newJumpLabel, ((JumpEqualConstant)inst).getConstantValue(), newLabel);
                }
                case JUMP_EQUAL_VARIABLE -> {
                    JumpEqualVariable jumpEqualVariable = (JumpEqualVariable) inst;
                    Variable jumpVariable = jumpEqualVariable.getVariableArgument();

                    yield new JumpEqualVariable(program, newVar, newJumpLabel, varsQuote.get(jumpVariable), newLabel);
                }
                case QUOTE -> {
                    Quote quote = (Quote) inst;
                    String name=((Quote) inst).getFunctionName();
                    String argument=((Quote) inst).getFunctionArguments();
                    yield new Quote(program,newVar,newLabel,name,argument);
                }
                case JUMP_EQUAL_FUNCTION -> {
                    String name=((JumpEqualFunction) inst).getFuncName();
                    String argument=((JumpEqualFunction) inst).getFuncArguments();
                    yield new JumpEqualFunction(program,newVar,newLabel,name,argument,newJumpLabel);
                }
            };
        } catch (IllegalArgumentException e) {
            // Include instruction name to help locate the failing node
            throw new ProgramLoadException("Failed to load program '" + inst.getName() + "': " + e.getMessage(), e);
        }

}

    private List<Instruction> expandAssignment(Assignment instruction) {
        // Implements v <- v1 using a work var z1 and three labels
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Variable v1 = instruction.getAssignedVariable();
        Variable z1 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel(), L2 = context.getFreshLabel(), L3 = context.getFreshLabel();

        return List.of(
                new ZeroVariable(program, v, instructionLabel),        // v = 0
                new JumpNotZero(program, v1, L1, FixedLabel.EMPTY),    // if v1 != 0 goto L1 else goto L3
                new GotoLabel(program, L3, FixedLabel.EMPTY),

                new Decrease(program, v1, L1),                         // L1: drain v1 into z1
                new Increase(program, z1, FixedLabel.EMPTY),
                new JumpNotZero(program, v1, L1, FixedLabel.EMPTY),

                new Decrease(program, z1, L2),                         // L2: restore v1 and fill v
                new Increase(program, v,FixedLabel.EMPTY),
                new Increase(program, v1, FixedLabel.EMPTY),
                new JumpNotZero(program, z1, L2, FixedLabel.EMPTY),

                new Neutral(program, v, L3)                            // L3: anchor
        );
    }

    private List<Instruction> expandConstantAssignment(ConstantAssignment instruction){
        // Implements v <- K as: v = 0; repeat K times: v++
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Long constant = instruction.getConstantValue();

        List<Instruction> out = new ArrayList<>();
        out.add(new ZeroVariable(program, v, instructionLabel));

        out.addAll(
                java.util.stream.LongStream.range(0, constant) // safe for K >= 0
                        .mapToObj(i -> new Increase(program, v, FixedLabel.EMPTY))
                        .toList()
        );

        return out;
    }

    private List<Instruction> expandGotoLabel(GotoLabel instruction) {
        // Unconditional jump via non-zero sentinel to trigger JNZ
        Variable z1 = context.getFreshWorkVal();
        Label jumpLabel = instruction.calculateInstruction();
        Label instructionLabel = instruction.getLabel();

        return List.of(
                new Increase(program, z1, instructionLabel),          // z1 = 1
                new JumpNotZero(program, z1, jumpLabel, FixedLabel.EMPTY) // jump
        );
    }

    private List<Instruction> expandJumpEqualsConstant(JumpEqualConstant instruction) {
        // Compare v to constant by copying to z1 and decrementing K times
        Variable v         = instruction.getVar();
        Label    jumpLabel = instruction.getJumpLabel();
        Long     constantL = instruction.getConstantValue();
        Label    instrLbl  = instruction.getLabel();

        if (constantL == null || constantL < 0) {
            throw new IllegalArgumentException("In command expandJumpEquals constant constant must be non-negative.\n");
        }
        long constant = constantL;

        Variable z1 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel();

        List<Instruction> out = new ArrayList<>();

        // z1 <- V
        out.add(new Assignment(program, z1, v, instrLbl));

        // K iterations: if z1 == 0 goto L1; else z1--
        Optional.of(constant)
                .filter(c -> c > 0)
                .ifPresent(c ->
                        LongStream.range(0, c)
                                .forEach(i -> {
                                    out.add(new JumpZero(program, z1, L1, FixedLabel.EMPTY));
                                    out.add(new Decrease(program, z1, FixedLabel.EMPTY));
                                })
                );

        // After loop: if z1 != 0 -> not equal
        out.add(new JumpNotZero(program, z1, L1, FixedLabel.EMPTY));

        // Equal → jump
        out.add(new GotoLabel(program, jumpLabel, FixedLabel.EMPTY));

        // L1: not equal anchor
         out.add(new Neutral(program, Variable.RESULT, L1));

        return out;
    }


    private List<Instruction> expandJumpEqualsVariable(JumpEqualVariable instruction) {
        // Simultaneously decrement copies z1,z2; equality iff both reach zero together
        Variable v         = instruction.getVar();
        Variable v1        = instruction.getVariableName();
        Label   instrLabel = instruction.getLabel();
        Label   jumpLabel  = instruction.getJumpLabel();

        Variable z1 = context.getFreshWorkVal();
        Variable z2 = context.getFreshWorkVal();
        Label L1 = context.getFreshLabel();
        Label L2 = context.getFreshLabel();
        Label L3 = context.getFreshLabel();

        return List.of(
                // z1 <- V, z2 <- V'
                new Assignment(program, z1, v,  instrLabel),
                new Assignment(program, z2, v1, FixedLabel.EMPTY),

                // L2: loop guard
                new Neutral(program, Variable.RESULT, L2),
                new JumpZero(program, z1, L3, FixedLabel.EMPTY), // if z1==0 break

                // if z2==0 -> not equal
                new JumpZero(program, z2, L1, FixedLabel.EMPTY),

                // z1-- ; z2-- ; goto L2
                new Decrease(program, z1, FixedLabel.EMPTY),
                new Decrease(program, z2, FixedLabel.EMPTY),
                new GotoLabel(program, L2, FixedLabel.EMPTY),

                // L3: if z2==0 -> equal else goto L1
                new Neutral(program, Variable.RESULT, L3),
                new JumpZero(program, z2, jumpLabel, L1),

                new Neutral(program, Variable.RESULT, L1) // not equal anchor
        );
    }


    private List<Instruction> expandJumpZero(JumpZero instruction) {
        // if v == 0 goto L, else fall through
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();
        Label jumpLabel = instruction.getJumpLabel();
        Label L1 = context.getFreshLabel();

        return List.of(
                new JumpNotZero(program, v, L1, instructionLabel), // if v!=0 skip jump
                new GotoLabel(program, jumpLabel, FixedLabel.EMPTY),
                new Neutral(program, Variable.RESULT, L1)          // anchor
        );
    }

    private List<Instruction> expandZeroVariable(ZeroVariable instruction) {
        // Set v to zero by looping: L1: v--; if v!=0 goto L1
        Variable v = instruction.getVar();
        Label instructionLabel = instruction.getLabel();

        // Reuse valid L* label if present; otherwise allocate fresh
        Label L1 = Optional.ofNullable(instructionLabel)
                .filter(l -> {
                    String t = l.getLabelRepresentation();
                    return t.matches("(?i)L\\d+") && Integer.parseInt(t.substring(1)) >= 1;
                }).orElseGet(context::getFreshLabel);

        return List.of(
                new Decrease(program, v, L1),
                new JumpNotZero(program, v, L1)
        );
    }

}
