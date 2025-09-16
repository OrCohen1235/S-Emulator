package logic.function;

import logic.instructions.Instruction;
import program.Program;

import java.util.*;

public class Function extends Program  {
   private final String userName;

   public Function(String name, String userName, List<Instruction> instructionList) {
       super.setNameOfProgram(name);
       this.userName = userName;
       for (Instruction instruction : instructionList) {
           instruction.setProgram(this);
       }
       super.getOriginalInstructions().addAll(instructionList);
       this.getProgramLoad().loadStartedVars();
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



}
