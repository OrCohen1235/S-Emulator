package menu.actions;

import menu.context.AppContext;
import util.InputHelper;

import java.util.List;
import java.util.Optional;

public class ExpandAction implements MenuAction {
    private final InputHelper input;

    public ExpandAction(InputHelper input) { this.input = input; }

    @Override public String title() { return "Expand"; }

    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        ctx.getEngineDTO().loadExpansion();
        int maxDegree = ctx.getEngineDTO().getMaxDegree();

        System.out.println("\nMax Degree is: " + maxDegree);
        ctx.setRunDegreeATM(input.askIntInRange(ctx.getIn(),
                "Choose a degree between 0 and " + maxDegree + " :", 0, maxDegree));

        Optional.of(ctx.getRunDegreeATM())
                .filter(degree -> degree > 0)
                .ifPresent(degree -> {
                    ctx.getEngineDTO().loadExpansionByDegree(degree);
                    ctx.getProgramDTO().setProgramViewToExpanded();
                });


        List<String> resultExpandCommands = ctx.getProgramDTO().getListOfExpandCommands();

        System.out.println("\nExpand commands: ");
        Optional.ofNullable(resultExpandCommands)
                .ifPresent(list -> list.forEach(System.out::println));

        ctx.getProgramDTO().setProgramViewToOriginal();
        ctx.getProgramDTO().resetMapVariables();
        ctx.getEngineDTO().resetSumOfCycles();
    }
}
