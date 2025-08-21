package menu.actions;

import menu.context.AppContext;
import util.InputHelper;

import java.util.List;

public class ExpandAction implements MenuAction {
    private final InputHelper input;

    public ExpandAction(InputHelper input) { this.input = input; }

    @Override public String title() { return "Expand"; }

    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        ctx.getEngineDTO().loadExpansion();
        int maxDegree = ctx.getEngineDTO().getMaxDegree();
        System.out.println("Max Degree is: " + maxDegree);
        ctx.setRunDegreeATM(input.askIntInRange(ctx.getIn(),
                "Choose a degree between 0 and " + maxDegree + " :", 0, maxDegree));
        if (ctx.getRunDegreeATM() > 0) {
            ctx.getEngineDTO().loadExpansionByDegree(ctx.getRunDegreeATM());
            ctx.getProgramDTO().setProgramViewToExpanded();
        }

        List<String> resultExpandCommands = ctx.getProgramDTO().getListOfExpandCommands();
        System.out.println("Expand commands: ");
        for (String s : resultExpandCommands) System.out.println(s);

        ctx.getProgramDTO().resetMapVariables();
        ctx.getEngineDTO().resetSumOfCycles();
    }
}
