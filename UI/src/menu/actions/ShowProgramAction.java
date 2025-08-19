package menu.actions;

import menu.context.AppContext;
import menu.view.ProgramPrinter;

import java.util.List;

public class ShowProgramAction implements MenuAction {
    private final ProgramPrinter printer;

    public ShowProgramAction(ProgramPrinter printer) { this.printer = printer; }

    @Override public String title() { return "Show program"; }

    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        List<String> expanded = ctx.getEngine().getListOfExpandCommands(ctx.getRunDegreeATM());
        printer.printProgram(ctx.getProgramDTO(), expanded);
    }
}
