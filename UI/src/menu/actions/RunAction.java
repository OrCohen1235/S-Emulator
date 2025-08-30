package menu.actions;

import logic.dto.EngineDTO;
import logic.dto.ProgramDTO;
import menu.context.AppContext;
import menu.context.HistoryContext;
import menu.view.ProgramPrinter;
import util.InputHelper;

import java.util.List;
import java.util.Optional;

public class RunAction implements MenuAction {
    private final InputHelper input;

    public RunAction(InputHelper input) { this.input = input; }

    @Override public String title() { return "Run program"; }
    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); } // Enabled only if a program is loaded

    @Override
    public void execute(AppContext ctx) {
        EngineDTO engineDTO = ctx.getEngineDTO();
        ProgramDTO programDTO = ctx.getProgramDTO();
        ShowProgramAction showProgramAction = new ShowProgramAction(new ProgramPrinter());

        engineDTO.loadExpansion(); // Precompute expansion metadata (degrees)
        System.out.println("\nMax Degree is: " + engineDTO.getMaxDegree());
        ctx.setRunDegreeATM(input.askIntInRange(ctx.getIn(), "Choose degree: ", 0, engineDTO.getMaxDegree())); // User picks degree

        Optional.of(ctx.getRunDegreeATM())
                .filter(degree -> degree != 0)
                .ifPresent(degree -> {
                    showProgramAction.setExpended(true); // Display expanded view
                    ctx.getEngineDTO().loadExpansionByDegree(degree); // Build flattened list by degree
                    ctx.getProgramDTO().setProgramViewToExpanded();   // Switch Program view
                });

        System.out.println("\nVariables:");
        programDTO.getVariables().forEach(variable ->
                System.out.println(variable)
        );

        List<Long> values = input.readCsvLongsFromUser(ctx.getIn()); // Read user inputs
        engineDTO.loadInputVars(values);                              // Load inputs into engine
        Long finalResult = engineDTO.runProgramExecutor(ctx.getRunDegreeATM()); // Execute program

        int sumOfCycles = engineDTO.getSumOfCycles(); // Collect total cycles
        updateHistory(values, ctx, finalResult);      // Persist run in history
        showProgramAction.execute(ctx);               // Print program (original/expanded as selected)
        System.out.println();
        programDTO.getVarsValues().forEach((name, val) ->
                System.out.println(name + " = " + val) // Print all variable values including Y
        );
        System.out.println("\nTotal Cycles: " + sumOfCycles);

        programDTO.resetMapVariables(); // Cleanup state for next run
        engineDTO.resetSumOfCycles();   // Reset cycle counter
    }

    private void updateHistory(List<Long> values, AppContext ctx, Long finalResult) {
        HistoryContext newHistoryContext = new HistoryContext(); // Record a single run
        newHistoryContext.setxValues(values);
        newHistoryContext.setDegree(ctx.getRunDegreeATM());
        newHistoryContext.setFinalResult(finalResult);
        newHistoryContext.setFinalCycles(ctx.getEngineDTO().getSumOfCycles());
        newHistoryContext.setNumberOfPrograms(ctx.getHistorySize() + 1);

        ctx.getHistoryContext().add(newHistoryContext);
        ctx.setHistorySize(ctx.getHistorySize() + 1);
    }
}
