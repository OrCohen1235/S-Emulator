package menu.actions;

import menu.context.AppContext;
import menu.context.HistoryContext;
import util.InputHelper;

import java.util.List;

public class RunAction implements MenuAction {
    private final InputHelper input;

    public RunAction(InputHelper input) { this.input = input; }

    @Override public String label() { return "Run program"; }
    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        ctx.engine.loadExpansion();
        System.out.println("Max Degree is: " + ctx.engine.getMaxDegree());
        ctx.runDegreeATM = input.askIntInRange(ctx.in, "Choose degree: ", 0, ctx.engine.getMaxDegree());

        if (ctx.runDegreeATM != 0) { ctx.engine.loadExpansionByDegree(ctx.runDegreeATM); }

        ctx.programDTO.getVariables().forEach(variable ->
                System.out.println("Variable: " + variable)
        );

        List<Long> values = input.readCsvLongsFromUser(ctx.in);
        ctx.engine.loadInputVars(values);

        Long finalResult = ctx.engine.runProgramExecutor(ctx.runDegreeATM);

        ctx.programDTO.getVarsValues().forEach((name, val) ->
                System.out.println("Variable: " + name + " = " + val)
        );
        System.out.println("\nTotal Cycles: " + ctx.engine.getSumOfCycles());

        HistoryContext newHistoryContext = new HistoryContext();
        newHistoryContext.setxValues(values);
        newHistoryContext.setDegree(ctx.runDegreeATM);
        newHistoryContext.setFinalResult(finalResult);
        newHistoryContext.setFinalCycles(ctx.engine.getSumOfCycles());
        newHistoryContext.setNumberofPrograms(ctx.historySize + 1);


        ctx.historyContext.add(newHistoryContext);
        ctx.engine.ResetSumOfCycles();
        ctx.historySize++;
    }
}
