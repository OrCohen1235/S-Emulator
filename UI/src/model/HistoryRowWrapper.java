// ה-Wrapper class:
package model;

public class HistoryRowWrapper {
    private final HistoryOldRow oldRow;
    private final HistoryRow newRow;

    public HistoryRowWrapper(HistoryOldRow oldRow, HistoryRow newRow) {
        this.oldRow = oldRow;
        this.newRow = newRow;
    }

    public HistoryOldRow getOldRow() { return oldRow; }
    public HistoryRow getNewRow() { return newRow; }
}