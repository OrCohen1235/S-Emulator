package menu.actions;

import menu.AppContext;

public interface MenuAction {
    String label();
    boolean enabled(AppContext ctx);
    void execute(AppContext ctx);
}
