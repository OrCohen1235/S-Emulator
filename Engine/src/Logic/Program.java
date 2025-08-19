package Logic;

import Logic.Instructions.BInstruction.Decrease;
import Logic.Instructions.BInstruction.Increase;
import Logic.Instructions.BInstruction.JumpNotZero;
import Logic.Instructions.BInstruction.Neutral;
import Logic.Instructions.Instruction;
import Logic.Instructions.InstructionData;
import Logic.Instructions.SInstruction.*;
import Logic.expansion.Expander;
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
    private Map<Variable, Long> xVariables = new LinkedHashMap();
    private Map<Variable, Long> zVariables = new LinkedHashMap();
    private Map<Variable, Long> y = new LinkedHashMap();
    private int countCycles = 0;

    private List<Instruction> expansionByDegree=new ArrayList<Instruction>();
    private int maxDegree = 0;


    public void loadProgram(ReadSemulatorXml read) {
        nameOfProgram = read.getProgramName();

        instructions.addAll(
                read.getSInstructionList().stream()
                        .map(this::createInstruction)
                        .toList()
        );
        y.put(Variable.RESULT, 0L);
    }

    public void loadInputVars(List<Long> varsInput) {
        IntStream.range(0, varsInput.size())
                .forEach(i -> {
                    Variable x = new VariableImpl(VariableType.INPUT, i + 1);
                    xVariables.put(x, varsInput.get(i));
                });
    }

    public void setInstructions(Instruction... instructions) {
        this.instructions.addAll(Arrays.asList(instructions));
    }

    public List<Instruction> getExpansionByDegree() {
        return expansionByDegree;
    }

    public void setExpansionByDegree(List<Instruction> instructions) {
        this.expansionByDegree.clear();
        this.expansionByDegree.addAll(instructions);
    }

    public void setMaxDegree(int maxDegree) {
        this.maxDegree = maxDegree;
    }

    public String getNameOfProgram() {
        return nameOfProgram;
    }

    public List<Instruction> getInstrutions() {
        return instructions;
    }

    public Long getXVirablesFromMap(Variable key) {
        return xVariables.computeIfAbsent(key, k -> 0L);
    }

    public Long getZVirablesFromMap(Variable key) {
        return zVariables.computeIfAbsent(key, k -> 0L);
    }

    public Long getY() {
        return y.get(Variable.RESULT);
    }

    public int getCountCycles() {
        return countCycles;
    }

    public void setxVirablesToMap(Variable keyVal, Long returnVal) {
        xVariables.put(keyVal, returnVal);
    }

    public void setzVirablesToMap(Variable keyVal, Long returnVal) {
        zVariables.put(keyVal, returnVal);
    }

    public void setY(Long value) {
        this.y.put(Variable.RESULT, value);
    }

    public Instruction getInstruction(int index) {
        return instructions.get(index);
    }

    public Instruction getInstructionByDegree(int index) {
        return expansionByDegree.get(index);
    }




    public Instruction getInstructionByLabel(Label label) {
      return instructions.stream()
              .filter(inst -> label.equals(inst.getLabel()))
              .findFirst().
              orElse(null);
   }

   public Instruction getInstructionByLabelFromDegreeList(Label label) {
      return expansionByDegree.stream()
              .filter(inst -> label.equals(inst.getLabel()))
              .findFirst().
              orElse(null);
   }

   public int getIndexInstruction(Instruction inst) {
      return instructions.indexOf(inst);
   }

   public int getIndexInstructionByDegree(Instruction inst) {
        int index = expansionByDegree.indexOf(inst);
      if (index == -1) {
          System.err.println("WARNING: Instruction " + inst + " not found in expansion by " + nameOfProgram);
      }
      return index;
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
            yield new GotoLabel(this, jumpLabel, newLabel);
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

    public Map<String, Long> getVariablesValues() {
        Map<String, Long> result = new LinkedHashMap<>();

        Long yVal = getY(); // או: y.get(Variable.RESULT)
        result.put(Variable.RESULT.getRepresentation(), yVal != null ? yVal : 0L);

        xVariables.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> parseIndex(e.getKey().getRepresentation(), 'x')))
                .forEach(e -> result.put(e.getKey().getRepresentation(),
                        e.getValue() != null ? e.getValue() : 0L));

        zVariables.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> parseIndex(e.getKey().getRepresentation(), 'z')))
                .forEach(e -> result.put(e.getKey().getRepresentation(),
                        e.getValue() != null ? e.getValue() : 0L));

        return result;
    }

    private int parseIndex(String name, char prefix) {
        if (name != null && name.length() > 1 && name.charAt(0) == prefix) {
            try {
                return Integer.parseInt(name.substring(1));
            } catch (NumberFormatException ignored) {}
        }
        return Integer.MAX_VALUE;
    }



}