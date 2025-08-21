package menu.actions;

import logic.dto.EngineDTO;
import logic.dto.ProgramDTO;
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
        ShowProgramAction showProgramAction = new ShowProgramAction(new ProgramPrinter());

        engineDTO.loadExpansion();
        System.out.println("Max Degree is: " + engineDTO.getMaxDegree());
        ctx.setRunDegreeATM(input.askIntInRange(ctx.getIn(), "Choose degree: ", 0, engineDTO.getMaxDegree()));
        if (ctx.getRunDegreeATM() != 0) {
            showProgramAction.setExpended(true);
            ctx.getEngineDTO().loadExpansionByDegree(ctx.getRunDegreeATM());
            ctx.getProgramDTO().setProgramViewToExpanded();
        }

        programDTO.getVariables().forEach(variable ->
                System.out.println("Variable: " + variable)
        );

        List<Long> values = input.readCsvLongsFromUser(ctx.getIn());
        engineDTO.loadInputVars(values);
        Long finalResult = engineDTO.runProgramExecutor(ctx.getRunDegreeATM());
        int sumOfCycles = engineDTO.getSumOfCycles();
        updateHistory(values, ctx, finalResult);
        showProgramAction.execute(ctx);
        programDTO.getVarsValues().forEach((name, val) ->
                System.out.println(name + " = " + val)
        );
        System.out.println("\nTotal Cycles: " + sumOfCycles);


        programDTO.resetMapVariables();
        engineDTO.resetSumOfCycles();

    }

    void updateHistory(List<Long> values, AppContext ctx, Long finalResult) {
        HistoryContext newHistoryContext = new HistoryContext();
        newHistoryContext.setxValues(values);
        newHistoryContext.setDegree(ctx.getRunDegreeATM());
        newHistoryContext.setFinalResult(finalResult);
        newHistoryContext.setFinalCycles(ctx.getEngineDTO().getSumOfCycles());
        newHistoryContext.setNumberOfPrograms(ctx.getHistorySize() + 1);

        ctx.getHistoryContext().add(newHistoryContext);
        ctx.setHistorySize(ctx.getHistorySize() + 1);
    }
}
