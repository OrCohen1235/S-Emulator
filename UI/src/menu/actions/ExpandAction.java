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
        ctx.getEngine().loadExpansion();
        int maxDegree = ctx.getEngine().getMaxDegree();
        System.out.println("Max Degree is: " + maxDegree);
        ctx.setRunDegreeATM(input.askIntInRange(ctx.getIn(),
                "Choose a degree between 0 and" + maxDegree + ":", 0, maxDegree));

        List<String> resultExpandCommands = ctx.getEngine().getListOfExpandCommands(ctx.getRunDegreeATM());
        System.out.println("Expand commands:");
        for (String s : resultExpandCommands) System.out.println(s);
    }
}
