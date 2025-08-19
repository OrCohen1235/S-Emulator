package menu.actions;

import menu.context.AppContext;

public interface MenuAction {
    String title();
    boolean enabled(AppContext ctx);
    void execute(AppContext ctx);
}
