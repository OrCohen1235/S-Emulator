package menu.actions;

import menu.context.AppContext;
import menu.context.HistoryContext;
import menu.view.ProgramPrinter;
import util.InputHelper;

import java.util.List;

public class RunAction implements MenuAction {
    private final InputHelper input;

    public RunAction(InputHelper input) { this.input = input; }

    @Override public String title() { return "Run program"; }
    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        ctx.getEngine().loadExpansion();
        System.out.println("Max Degree is: " + ctx.getEngine().getMaxDegree());
        ctx.setRunDegreeATM(input.askIntInRange(ctx.getIn(), "Choose degree: ", 0, ctx.getEngine().getMaxDegree()));

        if (ctx.getRunDegreeATM() != 0) { ctx.getEngine().loadExpansionByDegree(ctx.getRunDegreeATM()); }

        ctx.getProgramDTO().getVariables().forEach(variable ->
                System.out.println("Variable: " + variable)
        );

        List<Long> values = input.readCsvLongsFromUser(ctx.getIn());
        ctx.getEngine().loadInputVars(values);

        Long finalResult = ctx.getEngine().runProgramExecutor(ctx.getRunDegreeATM());
        ShowProgramAction showProgramAction = new ShowProgramAction(new ProgramPrinter());
        showProgramAction.execute(ctx);
        ctx.getProgramDTO().getVarsValues().forEach((name, val) ->
                System.out.println(name + " = " + val)
        );
        System.out.println("\nTotal Cycles: " + ctx.getEngine().getSumOfCycles());

        HistoryContext newHistoryContext = new HistoryContext();
        newHistoryContext.setxValues(values);
        newHistoryContext.setDegree(ctx.getRunDegreeATM());
        newHistoryContext.setFinalResult(finalResult);
        newHistoryContext.setFinalCycles(ctx.getEngine().getSumOfCycles());
        newHistoryContext.setNumberofPrograms(ctx.getHistorySize() + 1);

        ctx.getHistoryContext().add(newHistoryContext);
        ctx.getEngine().resetZMapVariables();
        ctx.getEngine().ResetSumOfCycles();
        ctx.setHistorySize(ctx.getHistorySize() + 1);
    }
}
