package logic.function;

import logic.instructions.Instruction;
import logic.variable.Variable;
import program.Program;

import java.util.*;

public class Function extends Program  {
   private final String userName;
   private Program mainProgram;

   public Function(String name, String userName, List<Instruction> instructionList) {
       super.setNameOfProgram(name);
       this.userName = userName;
       for (Instruction instruction : instructionList) {
           instruction.setProgram(this);
       }
       super.clearAndSetInstructions(instructionList);
       super.resetMapVariables();

   }

   public Variable getFreshWORK(){
       return mainProgram.getExpanderExecute().getFreshWork();
   }

   public Function(String name, List<Instruction> lst,List<Long> inputs){
       super.setNameOfProgram(name);
       this.userName = name;
       super.clearAndSetInstructions(lst);

   }

    public List<Instruction> getInstructionList() {
        return super.getActiveInstructions();
    }

    public String getUserName() {
        return userName;
    }

    public String getName(){
       return super.getName();
    }
    public void setMainProgram(Program mainProgram) {
       this.mainProgram = mainProgram;
    }

    public Program getMainProgram() {
        return mainProgram;
    }
}
