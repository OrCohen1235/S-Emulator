package Logic;

import Logic.BInstraction.Decrease;
import Logic.BInstraction.Increase;
import Logic.BInstraction.JumpNotZero;
import Logic.BInstraction.Neutral;
import Logic.label.Label;
import Logic.label.LabelImpl;
import Logic.variable.Variable;
import Logic.variable.VariableImpl;
import Logic.variable.VariableType;
import semulator.ReadSemulatorXml;
import semulator.SInstruction;

import java.util.*;

public class Program {
   private String nameOfProgram;
   private List<Instruction> instructions = new ArrayList<Instruction>();
   private Map<Variable, Long> xVirables = new LinkedHashMap();
   private Map<Variable, Long> zVirables = new LinkedHashMap();
   private Long y = 0L;
   private int countCycles = 0;

   public void loadProgram(ReadSemulatorXml read) {
      nameOfProgram = read.getProgramName();
      List<SInstruction> inputList = read.getSInstructionList();

      for (SInstruction tmpInput : inputList) {
         Variable newVar  = new VariableImpl(tmpInput.getSVariable());
         Label newLabel;
         if (tmpInput.getSLabel()!=null) {
            newLabel = new LabelImpl(tmpInput.getSLabel());
         }
         else{
            newLabel= new LabelImpl("EMPTY");
         }
         System.out.println(InstructionData.fromName(tmpInput.getName()));
         Instruction instr = switch (InstructionData.fromName(tmpInput.getName())) {
            case INCREASE -> new Increase(this, newVar, newLabel);
            case DECREASE -> new Decrease(this, newVar, newLabel);
            case Neutral    -> new Neutral(this, newVar, newLabel);
            case JUMP_NOT_ZERO -> {
               String target = tmpInput.getSInstructionArguments()
                       .getSInstructionArgument()
                       .get(0)
                       .getValue();
               Label jumpLabel = new LabelImpl(target); // <-- גם כאן קונסטרקטור
               yield new JumpNotZero(this, newVar, jumpLabel, newLabel);
            }
         };

         instructions.add(instr);
      }
   }

   public void loadInputVars(Long... vars ) {
   int i = 1;
   for (Long var : vars) {
      Variable x=new VariableImpl(VariableType.INPUT,i);
      i++;
      xVirables.put(x, var);
   }
}
   public void setInstructions(Instruction... instructions) {
      this.instructions.addAll(Arrays.asList(instructions));
   }



   public String getNameOfProgram() {
      return nameOfProgram;
   }

   public List<Instruction> getInstractions() {
      return instructions;
   }

   public Long getXVirablesFromMap(Variable key) {
      System.out.println(xVirables.get(key));
      return xVirables.get(key);
   }

   public Long getZVirablesFromMap(Variable key) {
      return zVirables.get(key);
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

   public Instruction getInstraction(int index) {
      return instructions.get(index);
   }

   public Instruction getInstractionByLabel(Label label) {
      for (Instruction inst : instructions) {
         if (label.equals(inst.getLabel())) {
            return inst;
         }
      }
      return null;
   }

   public int getIndexInstraction(Instruction inst) {
      return instructions.indexOf(inst);
   }



}