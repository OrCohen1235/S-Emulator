package Logic;

import Logic.label.Label;
import Logic.variable.Variable;
import Logic.variable.VariableImpl;
import Logic.variable.VariableType;

import java.util.*;

public class Program {
   private String nameOfProgram;
   private List<Instruction> instructions = new ArrayList<Instruction>();
   private Map<Variable, Long> xVirables = new LinkedHashMap();
   private Map<Variable, Long> zVirables = new LinkedHashMap();
   private Long y = 0L;
   private int countCycles = 0;

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