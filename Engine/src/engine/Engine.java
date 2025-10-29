package engine;

import logic.dto.ProgramDTO;
import logic.instructions.Instruction;
import program.*;
import program.ProgramLoadException;
import logic.execution.ProgramExecutorImpl;
import logic.expansion.ExpanderExecute;
import jaxbsprogram.ReadSemulatorXml;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class Engine {

    // -------------------- Fields --------------------
    private  ReadSemulatorXml readSem; // Responsible for reading XML file
    private  Program program;          // Represents the program itself
    private  ProgramDTO programDTO;    // DTO object for program data transfer
    private Boolean isLoaded = false;       // Indicates if program was successfully loaded

    // -------------------- Constructor --------------------
    public Engine(InputStream xmlFileContext) {
        try {
            readSem = new ReadSemulatorXml(xmlFileContext);

            String label = readSem.checkLabelValidity();
            if (!Objects.equals(label, "")){
                // Thrown if there is a jump to a non-existing label
                throw new ProgramLoadException("There is a jump command to label " + label + " that does not exist in the program.");
            }

            String functionError = readSem.checkFunctionValidity();
            if (!Objects.equals(functionError, "")) {
                // Thrown if there is a reference to a non-existing function
                throw new ProgramLoadException(functionError);
            }

            isLoaded = true; // Program loaded successfully

        } catch (Exception e) {
            throw e; // Re-throw original exception
        }
        program = new Program(readSem,true);
        programDTO = new ProgramDTO(program);
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public Engine(){};




    // -------------------- Basic accessors --------------------
    public Boolean getLoaded() { return isLoaded; }

    public ProgramDTO getProgramDTO() { return programDTO; }

    public Program getProgram() {
        return program;
    }

    // -------------------- Run / Inputs --------------------

}
