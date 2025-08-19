package menu.actions;

import menu.context.AppContext;

public class ExitAction implements MenuAction {
    @Override public String label() { return "Exit"; }
    @Override public boolean enabled(AppContext ctx) { return true; }

    @Override
    public void execute(AppContext ctx) {
        System.out.println("Bye!");
        System.exit(0);
    }
}
