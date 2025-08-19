package menu.actions;

import Logic.DTO.EngineDTO;
import Logic.DTO.ProgramDTO;
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
        EngineDTO engineDTO = ctx.getEngineDTO();
        ProgramDTO programDTO = ctx.getProgramDTO();

        engineDTO.loadExpansion();
        System.out.println("Max Degree is: " + engineDTO.getMaxDegree());
        ctx.setRunDegreeATM(input.askIntInRange(ctx.getIn(), "Choose degree: ", 0, engineDTO.getMaxDegree()));

        if (ctx.getRunDegreeATM() != 0) { engineDTO.loadExpansionByDegree(ctx.getRunDegreeATM()); }

        programDTO.getVariables().forEach(variable ->
                System.out.println("Variable: " + variable)
        );

        List<Long> values = input.readCsvLongsFromUser(ctx.getIn());
        engineDTO.loadInputVars(values);

        Long finalResult = engineDTO.runProgramExecutor(ctx.getRunDegreeATM());
        ShowProgramAction showProgramAction = new ShowProgramAction(new ProgramPrinter());
        showProgramAction.execute(ctx);
        programDTO.getVarsValues().forEach((name, val) ->
                System.out.println(name + " = " + val)
        );
        System.out.println("\nTotal Cycles: " + programDTO.getNumOfCycles());

        programDTO.resetZMapVariables();
        engineDTO.resetSumOfCycles();
        updateHistory(values, ctx, finalResult);
    }

    void updateHistory(List<Long> values, AppContext ctx, Long finalResult) {
        HistoryContext newHistoryContext = new HistoryContext();
        newHistoryContext.setxValues(values);
        newHistoryContext.setDegree(ctx.getRunDegreeATM());
        newHistoryContext.setFinalResult(finalResult);
        newHistoryContext.setFinalCycles(ctx.getProgramDTO().getNumOfCycles());
        newHistoryContext.setNumberOfPrograms(ctx.getHistorySize() + 1);

        ctx.getHistoryContext().add(newHistoryContext);
        ctx.setHistorySize(ctx.getHistorySize() + 1);
    }
}
