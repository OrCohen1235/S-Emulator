package menu.actions;

import menu.context.AppContext;
import util.InputHelper;

import java.util.List;

public class ExpandAction implements MenuAction {
    private final InputHelper input;

    public ExpandAction(InputHelper input) { this.input = input; }

    @Override public String label() { return "Expand"; }
    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        ctx.engine.loadExpansion();
        int maxDegree = ctx.engine.getMaxDegree();
        System.out.println("Max Degree is: " + maxDegree);
        ctx.runDegreeATM = input.askIntInRange(ctx.in,
                "Choose a degree between 0 and:" + maxDegree, 0, maxDegree);

        List<String> resultExpandCommands = ctx.engine.getListOfExpandCommands(ctx.runDegreeATM);
        System.out.println("Expand commands:");
        for (String s : resultExpandCommands) System.out.println(s);
    }
}
