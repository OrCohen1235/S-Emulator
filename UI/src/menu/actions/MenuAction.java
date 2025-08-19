package menu.actions;

import menu.context.AppContext;

public interface MenuAction {
    String label();
    boolean enabled(AppContext ctx);
    void execute(AppContext ctx);
}
