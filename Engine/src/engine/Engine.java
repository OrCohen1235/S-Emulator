package engine;

import Logic.DTO.ProgramDTO;
import Logic.Program;
import Logic.execution.ProgramExecutorImpl;
import semulator.ReadSemulatorXml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Engine {
    private ReadSemulatorXml readSem;
    private final Program program;
    private final ProgramDTO programDTO;
    private Boolean isLoaded=false;
    private final ProgramExecutorImpl programExecutor;


    public Engine(File file){
         readSem = new ReadSemulatorXml(file);
         program=new Program();
         program.loadProgram(readSem);
         programDTO=new ProgramDTO(program);
         programExecutor=new ProgramExecutorImpl(program);
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
    public void loadInputVars(ArrayList<Long> input){
        program.loadInputVars(input);
    }

}
