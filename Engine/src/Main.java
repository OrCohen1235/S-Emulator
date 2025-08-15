import Logic.BInstraction.Decrease;
import Logic.BInstraction.Increase;
import Logic.BInstraction.JumpNotZero;
import Logic.BInstraction.Neutral;
import Logic.Program;
import Logic.Instruction;
import Logic.execution.ProgramExecutorImpl;
import Logic.label.FixedLabel;
import Logic.label.Label;
import Logic.label.LabelImpl;
import Logic.variable.Variable;
import Logic.variable.VariableImpl;
import Logic.variable.VariableType;
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

        String name = "C:\\Users\\orcoh\\source\\repos\\S-Emulator\\S-Emuletor\\Engine\\src\\semulator\\badic.xml";
        File xmlFIle = new File(name);
        ReadSemulatorXml read = new ReadSemulatorXml(xmlFIle);

        program.loadProgram(read);
        program.loadInputVars(8L);

        ProgramExecutorImpl pRun=new ProgramExecutorImpl(program);
        Object ress = pRun.run();
        System.out.println(ress);



    }
}
