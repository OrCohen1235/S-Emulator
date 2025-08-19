package menu.view;

import menu.History;

import java.util.List;

public class HistoryPrinter {
    public void printList(List<History> history) {
        if (history == null || history.isEmpty()) {
            System.out.println("No runs recorded for the current program.");
            return;
        }
        for (History h : history) {
            System.out.println("===== Run History Entry =====");
            System.out.println("Run #:      " + h.getNumberofPrograms());
            System.out.println("Degree:     " + h.getDegree());
            System.out.println("Inputs:     " + formatInputs(h.getxValues()));
            System.out.println("y (result): " + (h.getFinalResult() == null ? "null" : h.getFinalResult()));
            System.out.println("Cycles:     " + h.getFinalCycles());
            System.out.println("=============================");
        }
    }

    private String formatInputs(java.util.List<Long> xs) {
        if (xs == null || xs.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < xs.size(); i++) {
            if (i > 0) sb.append(',');
            Long v = xs.get(i);
            sb.append(v == null ? "null" : v.toString());
        }
        return sb.toString();
    }
}
