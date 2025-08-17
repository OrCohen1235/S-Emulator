package engine;

import Logic.DTO.ProgramDTO;
import Logic.Instructions.Instruction;
import Logic.Instructions.SInstruction.SyntheticInstruction;
import Logic.Program;
import Logic.execution.ProgramExecutorImpl;
import Logic.expansion.Expander;
import Logic.expansion.ExpansionContext;
import semulator.ReadSemulatorXml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.stream;

public class Engine {
    private ReadSemulatorXml readSem;
    private final Program program;
    private final ProgramDTO programDTO;
    private Boolean isLoaded=false;
    private final ProgramExecutorImpl programExecutor;
    private ExpansionContext expansionContext;
    Expander expander;


    public Engine(File file){
         readSem = new ReadSemulatorXml(file);
         program=new Program();
         program.loadProgram(readSem);
         programDTO=new ProgramDTO(program);
         programExecutor=new ProgramExecutorImpl(program);
         expansionContext=new ExpansionContext(program,1,getMaxLabelNumber()+1);
         expander=new Expander(expansionContext);
    }
    public void getExpandedInstructions() {

        List<Instruction> lst = expander.expand(program.getInstruction(0));
        for (Instruction i : lst) {
            System.out.println("\n"+i.getCommand());
        }

        List<Instruction> lst1 = expander.expand(program.getInstruction(1));
        for (Instruction j : lst1) {
            System.out.println("\n"+j.getCommand());
        }
    }
    public int getMaxLabelNumber() {
        return programDTO.getLabels().stream()                 // או printLabels() אם זה השם אצלך
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(lbl -> !"EXIT".equalsIgnoreCase(lbl))
                .filter(lbl -> lbl.startsWith("L")
                        && lbl.length() > 1
                        && lbl.substring(1).chars().allMatch(Character::isDigit))
                .mapToInt(lbl -> Integer.parseInt(lbl.substring(1)))
                .max()
                .orElse(0);
    }


    public Boolean getLoaded() {
        return isLoaded;
    }
    public void setLoaded(Boolean isLoaded) {
        this.isLoaded = isLoaded;
    }

    public ProgramDTO getProgramDTO() {
        return programDTO;
    }
    public Long runProgramExecutor(){
        return programExecutor.run();
    }
    public void loadInputVars(List<Long> input){
        program.loadInputVars(input);
    }

    public void loadExpasion(){
        loadExpansionRecursive(program.getInstrutions());
    }
    public void loadExpansionRecursive(List<Instruction> ListOfexpansion)
    {
        if (ListOfexpansion.size()== 1) {
        }
        else {
            for (Instruction i : ListOfexpansion) {
                    List<Instruction> lst = expander.expand(i);
                    loadExpansionRecursive(lst);
                    if (i instanceof SyntheticInstruction) {
                        i.setDegree(getMaxDegreeRecursive(lst) + 1);
                    }
                }
            }
    }

    public int getMaxDegree()
    {
        return getMaxDegreeRecursive(program.getInstrutions());
    }
    public int getMaxDegreeRecursive(List<Instruction> instList)
    {
        int maxDegree = 0 ;
        for (Instruction inst : instList){
            if (maxDegree<inst.getDegree()){
                maxDegree = inst.getDegree();
            }
        }
        return maxDegree;
    }




}
