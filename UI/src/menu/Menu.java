package menu;

import Logic.DTO.EngineDTO;
import menu.actions.*;
import menu.context.AppContext;
import menu.view.HistoryPrinter;
import menu.view.MainMenuPrinter;
import menu.view.ProgramPrinter;
import util.InputHelper;

import java.util.List;
import java.util.Scanner;

public class Menu {
    private final AppContext ctx = new AppContext(new Scanner(System.in));
    private final InputHelper input = new InputHelper();
    private final MainMenuPrinter mainMenuPrinter = new MainMenuPrinter();

    private final List<MenuAction> actions = List.of(
            new LoadXmlAction(input),                  // 1
            new ShowProgramAction(new ProgramPrinter()), // 2
            new ExpandAction(input),                   // 3
            new RunAction(input),                      // 4
            new ShowHistoryAction(new HistoryPrinter()), // 5
            new ExitAction()                           // 6
    );

    public Menu() {}

    public void run() {
        while (true) {
            mainMenuPrinter.print(ctx);
            int choice = input.askIntInRange(ctx.getIn(), "Choose an option (1-6): \n", 1, 6);
            MenuAction action = actions.get(choice - 1);

            if (!action.enabled(ctx)) {
                System.out.println("No valid program is currently loaded.");
                continue;
            }
            action.execute(ctx);
            System.out.println();
        }
    }
}
