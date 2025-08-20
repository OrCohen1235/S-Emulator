import Logic.DTO.ProgramDTO;
import Program.Program;
import Logic.execution.ProgramExecutorImpl;
import semulator.ReadSemulatorXml;

import java.io.File;

public class Main {

    public static void main(String[] args) {
        Program program = new Program();
        /*Variable x1 = new VariableImpl(VariableType.INPUT, 1);
        Variable x2 = new VariableImpl(VariableType.INPUT, 2);
        Variable x3 = new VariableImpl(VariableType.INPUT, 3);
        Variable y = new VariableImpl(VariableType.RESULT,0);
        Label L1= new LabelImpl(1);
        Label L2= new LabelImpl(2);
        Instruction inst1 = new Increase(program, x1, L1);
        Instruction inst2= new Decrease(program, x2, L2);
        Instruction inst3= new Neutral(program, x3);
        Instruction inst5=new Increase(program, y);
        Instruction inst6 = new JumpNotZero(program, y, FixedLabel.EXIT);
        program.setInstructions(inst1, inst2, inst3,inst5, inst6);*/

        //String name = "C:\\Users\\orcoh\\source\\repos\\S-Emulator\\S-Emuletor\\Engine\\src\\semulator\\badic.xml";
        //String name = "C:\\Users\\orcoh\\source\\repos\\S-Emulator\\S-Emuletor\\Engine\\src\\semulator\\synthetic.xml";
        String name = "/Users/yuvalharel/Desktop/ degree/second year/Java/S-Emulator/Engine/src/semulator/synthetic.xml";
        File xmlFIle = new File(name);
        ReadSemulatorXml read = new ReadSemulatorXml(xmlFIle);

        program.loadProgram(read);
       // program.loadInputVars(8L, 6L, 1L, 2L, 3L, 4L, 5L);
        ProgramDTO PrintEverything = new ProgramDTO(program);
        //printDetails(PrintEverything);

        ProgramExecutorImpl pRun=new ProgramExecutorImpl(program);
        Object ress = pRun.run();
        System.out.println(ress);


    }




}
