package menu.actions;

import menu.AppContext;
import menu.view.ProgramPrinter;

import java.util.List;

public class ShowProgramAction implements MenuAction {
    private final ProgramPrinter printer;

    public ShowProgramAction(ProgramPrinter printer) { this.printer = printer; }

    @Override public String label() { return "Show program"; }
    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        List<String> expanded = ctx.engine.getListOfExpandCommands(ctx.runDegreeATM);
        printer.printProgram(ctx.programDTO, expanded);
    }
}
