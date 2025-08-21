package menu.actions;

import menu.context.AppContext;
import menu.view.ProgramPrinter;

import java.util.List;

public class ShowProgramAction implements MenuAction {
    private final ProgramPrinter printer;
    private boolean isExpended =  false;

    public ShowProgramAction(ProgramPrinter printer) { this.printer = printer; }

    @Override public String title() { return "Show program"; }

    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        if (isExpended) { ctx.getProgramDTO().setProgramViewToExpanded();}
        else { ctx.getProgramDTO().setProgramViewToOriginal(); }

        List<String> expanded = ctx.getProgramDTO().getListOfExpandCommands();
        printer.printProgram(ctx.getProgramDTO(), expanded);

        ctx.getEngineDTO().resetSumOfCycles();
    }

    public void setExpended(boolean expended) {
        isExpended = expended;
    }
}
