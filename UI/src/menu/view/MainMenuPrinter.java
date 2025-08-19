package menu.view;

import menu.AppContext;

public class MainMenuPrinter {
    public void print(AppContext ctx) {
        boolean hasProgram = ctx.hasProgram();
        System.out.println("=====================================");
        System.out.println("Menu:");
        System.out.println("1) Load XML");
        System.out.println("2) Show program"   + (hasProgram ? "" : "  (disabled: no program loaded)"));
        System.out.println("3) Expand"         + (hasProgram ? "" : "            (disabled: no program loaded)"));
        System.out.println("4) Run program"    + (hasProgram ? "" : "      (disabled: no program loaded)"));
        System.out.println("5) Show history"   + (hasProgram ? "" : "     (disabled: no program loaded)"));
        System.out.println("6) Exit");
    }
}
