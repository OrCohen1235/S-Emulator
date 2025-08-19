package menu.actions;

import menu.context.AppContext;
import menu.view.HistoryPrinter;

public class ShowHistoryAction implements MenuAction {
    private final HistoryPrinter printer;

    public ShowHistoryAction(HistoryPrinter printer) { this.printer = printer; }

    @Override public String label() { return "Show history"; }
    @Override public boolean enabled(AppContext ctx) { return ctx.hasProgram(); }

    @Override
    public void execute(AppContext ctx) {
        printer.printList(ctx.historyContext);
    }
}
