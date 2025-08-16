package Logic;

import Logic.BInstruction.Decrease;
import Logic.BInstruction.Increase;
import Logic.BInstruction.JumpNotZero;
import Logic.BInstruction.Neutral;
import Logic.SInstruction.*;
import Logic.label.Label;
import Logic.label.LabelImpl;
import Logic.variable.Variable;
import Logic.variable.VariableImpl;
import Logic.variable.VariableType;
import semulator.ReadSemulatorXml;
import semulator.SInstruction;

import java.util.*;
import java.util.stream.IntStream;

public class Program {
   private String nameOfProgram;
   private List<Instruction> instructions = new ArrayList<Instruction>();
   private Map<Variable, Long> xVirables = new LinkedHashMap();
   private Map<Variable, Long> zVirables = new LinkedHashMap();
   private Long y = 0L;
   private int countCycles = 0;

   public void loadProgram(ReadSemulatorXml read) {
      nameOfProgram = read.getProgramName();

      instructions.addAll(
              read.getSInstructionList().stream()
                      .map(this::createInstruction)
                      .toList()
      );
   }

   public void loadInputVars(Long... vars ) {
      IntStream.range(0, vars.length)
              .forEach(i -> {
                 Variable x = new VariableImpl(VariableType.INPUT, i + 1);
                 xVirables.put(x, vars[i]);
              });
   }

   public void setInstructions(Instruction... instructions) {
      this.instructions.addAll(Arrays.asList(instructions));
   }

   public String getNameOfProgram() {
      return nameOfProgram;
   }

   public List<Instruction> getInstrutions() {
      return instructions;
   }

   public Long getXVirablesFromMap(Variable key) {
      return xVirables.computeIfAbsent(key, k -> 0L);
   }

   public Long getZVirablesFromMap(Variable key) {
      return zVirables.computeIfAbsent(key, k -> 0L);
   }

   public Long getY() {
      return y;
   }

   public int getCountCycles() {
      return countCycles;
   }

   public void setxVirablesToMap(Variable keyVal,Long returnVal) {
      xVirables.put(keyVal, returnVal);
   }

   public void setzVirablesToMap(Variable keyVal,Long returnVal) {
      zVirables.put(keyVal, returnVal);
   }

   public void setY(Long y) {
      this.y = y;
   }

   public Instruction getInstruction(int index) {
      return instructions.get(index);
   }

   public Instruction getInstructionByLabel(Label label) {
      return instructions.stream()
              .filter(inst -> label.equals(inst.getLabel()))
              .findFirst().
              orElse(null);
   }

   public int getIndexInstruction(Instruction inst) {
      return instructions.indexOf(inst);
   }

   public String getArgument(SInstruction inst) {
      return inst.getSInstructionArguments()
              .getSInstructionArgument()
              .getFirst()
              .getValue();
   }

   private String getArgumentByName(SInstruction inst, String name) {
      return inst.getSInstructionArguments().getSInstructionArgument().stream()
              .filter(a -> name.equals(a.getName()))
              .map(a -> a.getValue())
              .findFirst()
              .orElseThrow(() -> new IllegalArgumentException("Missing argument: " + name));
   }

   public Instruction createInstruction(SInstruction inst) {
      Variable newVar  = new VariableImpl(inst.getSVariable());

      Label newLabel = Optional.ofNullable(inst.getSLabel())
              .map(LabelImpl::new)
              .orElse(new LabelImpl("EMPTY"));

      return switch (InstructionData.fromName(inst.getName())) {
         case INCREASE -> new Increase(this, newVar, newLabel);
         case DECREASE -> new Decrease(this, newVar, newLabel);
         case NEUTRAL -> new Neutral(this, newVar, newLabel);
         case JUMP_NOT_ZERO -> {
            Label jumpLabel = new LabelImpl(getArgument(inst));
            yield new JumpNotZero(this, newVar, jumpLabel, newLabel);
         }
         case ASSIGNMENT -> {
            Variable assignmentVariable = new VariableImpl(getArgument(inst));
            yield new Assignment(this, newVar, assignmentVariable, newLabel);
         }
         case CONSTANT_ASSIGNMENT -> {
            Long constant = Long.parseLong(getArgument(inst));
            yield new ConstantAssignment(this, newVar, constant, newLabel);
         }
         case GOTO_LABEL -> {
            Label jumpLabel = new LabelImpl(getArgument(inst));
            yield new GotoLabel(this, newVar, jumpLabel, newLabel);
         }
         case JUMP_ZERO -> {
            Label jumpLabel = new LabelImpl(getArgument(inst));
            yield new JumpZero(this, newVar, jumpLabel, newLabel);
         }
         case ZERO_VARIABLE -> new ZeroVariable(this, newVar, newLabel);
         case JUMP_EQUAL_CONSTANT -> {
            Label jumpLabel = new LabelImpl(getArgumentByName(inst, "JEConstantLabel"));
            Long constant   = Long.parseLong(getArgumentByName(inst, "constantValue"));
            yield new JumpEqualConstant(this, newVar, jumpLabel, constant, newLabel);
         }
         case JUMP_EQUAL_VARIABLE -> {
            Label jumpLabel   = new LabelImpl(getArgumentByName(inst, "JEVariableLabel"));
            Variable variable = new VariableImpl(getArgumentByName(inst, "variableName"));
            yield new JumpEqualVariable(this, newVar, jumpLabel, variable, newLabel);
         }
      };
   }
}