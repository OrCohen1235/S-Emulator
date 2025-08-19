package menu.actions;

import menu.AppContext;
import menu.History;
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

        History newHistory = new History();
        newHistory.setxValues(values);
        newHistory.setDegree(ctx.runDegreeATM);
        newHistory.setFinalResult(finalResult);
        newHistory.setFinalCycles(ctx.engine.getSumOfCycles());
        newHistory.setNumberofPrograms(ctx.historySize + 1);


        ctx.history.add(newHistory);
        ctx.engine.ResetSumOfCycles();
        ctx.historySize++;
    }
}
