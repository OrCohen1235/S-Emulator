package menu;

import menu.actions.*;
import menu.context.AppContext;
import menu.view.HistoryPrinter;
import menu.view.MainMenuPrinter;
import menu.view.ProgramPrinter;
import util.InputHelper;

import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Menu {
    private final AppContext ctx = new AppContext(new Scanner(System.in)); // Consider owning/closing Scanner in main and passing it in
    private final InputHelper input = new InputHelper();                    // Could be injected for easier testing
    private final MainMenuPrinter mainMenuPrinter = new MainMenuPrinter();  // Printer responsibility is well separated

    private final List<MenuAction> actions = List.of(
            new LoadXmlAction(input),                  // 1
            new ShowProgramAction(new ProgramPrinter()), // 2
            new ExpandAction(input),                   // 3
            new RunAction(input),                      // 4
            new ShowHistoryAction(new HistoryPrinter()), // 5
            new ExitAction()                           // 6
    ); // Tip: consider using an enum to pair indexes/titles with actions for more explicit mapping

    public Menu() {}

    public void run() {
        // Suggestion: wrap loop body with try/catch to prevent a single action failure from killing the loop
        // Suggestion: maintain a 'running' boolean and let ExitAction flip it, instead of hard infinite loop
        while (true) {
            mainMenuPrinter.print(ctx); // Nice: single responsibility for UI
            int choice = input.askIntInRange(ctx.getIn(), "Choose an option (1-6): ", 1, 6); // Good: validated input
            MenuAction action = actions.get(choice - 1); // Index-based dispatch; enum/switch could be clearer

            if (!action.enabled(ctx)) {
                System.out.println("No valid program is currently loaded.\n"); // UX: consider showing which options are disabled in the menu
                continue;
            }

            // Idea: measure and log action execution time for profiling long runs
            action.execute(ctx); // Consider returning a boolean to indicate "should exit" (e.g., ExitAction)
            try {
                TimeUnit.SECONDS.sleep(2); // UX: make this configurable or skip after fast CLI interactions
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Correct: restore interrupt status
                // Optional: break the loop if interruption should stop the app
            }
        }
    }
}
