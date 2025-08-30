package menu.actions;

import logic.dto.EngineDTO;
import program.ProgramLoadException;
import menu.context.AppContext;
import util.InputHelper;

import java.io.File;

import static java.lang.System.in;

public class LoadXmlAction implements MenuAction {
    private final InputHelper input; // Reads and validates user input (file path)

    public LoadXmlAction(InputHelper input) { this.input = input; }

    @Override public String title() { return "Load XML"; } // Menu label

    @Override public boolean enabled(AppContext ctx) { return true; } // Always enabled

    @Override
    public void execute(AppContext ctx) {
        try {
            System.out.print("\nEnter full path to XML file (leave empty to cancel): ");
            String path = input.readValidXmlPathOrCancel(ctx.getIn());
            EngineDTO temp = new EngineDTO(path); // Probe load to validate XML and semantics
            if (temp.getLoaded()) {
                EngineDTO currentEngineDTO = new EngineDTO(path); // Reload for actual use
                ctx.setEngineDTO(currentEngineDTO);            // Store engine in app context
                ctx.setProgramDTO(currentEngineDTO.getProgramDTO()); // Expose ProgramDTO in context
                System.out.println("\nProgram loaded successfully: " + ctx.getProgramDTO().getProgramName() );
            } else {
                System.out.println("Invalid XML (application-wise). The previous valid program remains active."); // Keep previous program
            }
        } catch (ProgramLoadException e){
            System.out.println(e.getMessage() + "\n"); // Domain-specific load error details
        } catch (Exception e) {
            System.out.println("Failed to load XML: " + e.getClass().getSimpleName() +
                    (e.getMessage() != null ? " - " + e.getMessage() : "")); // Generic failure path
            System.out.println("The previous valid program (if existed) remains active.\n"); // Do not override old state
        }
    }
}
