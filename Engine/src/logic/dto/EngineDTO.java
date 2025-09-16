package logic.dto;

import engine.Engine;

import java.util.List;

public class EngineDTO {
    private final Engine engine; // Wraps the Engine instance

    public EngineDTO(String filePath) {
        this.engine = new Engine(filePath); // Initialize Engine with XML file
    }

    public boolean getLoaded (){
        return engine.getLoaded(); // Check if program was successfully loaded
    }

    public ProgramDTO getProgramDTO() {
        return engine.getProgramDTO(); // Return program DTO
    }

}
