package menu.actions;

import logic.dto.EngineDTO;
import program.ProgramLoadException;
import menu.context.AppContext;
import util.InputHelper;

import java.io.File;

public class LoadXmlAction implements MenuAction {
    private final InputHelper input;

    public LoadXmlAction(InputHelper input) { this.input = input; }

    @Override public String title() { return "Load XML"; }
    @Override public boolean enabled(AppContext ctx) { return true; }

    @Override
    public void execute(AppContext ctx) {
        String path = input.readValidXmlPathOrCancel(ctx.getIn());
        if (path == null) return;

        File f = new File(path);
        try {
            EngineDTO temp = new EngineDTO(f);
            if (temp.getLoaded()) {
                EngineDTO currentEngineDTO = new EngineDTO(f);
                ctx.setEngineDTO(currentEngineDTO);
                ctx.setProgramDTO(currentEngineDTO.getProgramDTO());
                System.out.println("Program loaded successfully: " + ctx.getProgramDTO().getProgramName() + "\n");
            } else {
                System.out.println("Invalid XML (application-wise). The previous valid program remains active.\n");
            }
        } catch (ProgramLoadException e){
            System.out.println(e.getMessage() + "\n");
        } catch (Exception e) {
            System.out.println("Failed to load XML: " + e.getClass().getSimpleName() +
                    (e.getMessage() != null ? " - " + e.getMessage() : ""));
            System.out.println("The previous valid program (if existed) remains active.\n");
        }
    }
}
