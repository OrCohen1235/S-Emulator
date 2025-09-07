package ui.viewmodel;

import javafx.beans.property.*;
public class DebugSessionViewModel {
    private final BooleanProperty debugging = new SimpleBooleanProperty(false);
    private final IntegerProperty debuggerLevel = new SimpleIntegerProperty(0);
    private final IntegerProperty highlightedRowIndex = new SimpleIntegerProperty(-1);
    private final IntegerProperty lastVisibleIndex = new SimpleIntegerProperty(-1);

    public BooleanProperty debuggingProperty() { return debugging; }
    public IntegerProperty debuggerLevelProperty() { return debuggerLevel; }
    public IntegerProperty highlightedRowIndexProperty() { return highlightedRowIndex; }
    public IntegerProperty lastVisibleIndexProperty() { return lastVisibleIndex; }

    public boolean isAtEnd() {
        int sel = highlightedRowIndex.get();
        int last = lastVisibleIndex.get();
        return sel >= 0 && sel == last;
    }
}
