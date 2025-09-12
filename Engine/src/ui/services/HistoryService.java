package ui.services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ui.model.HistoryRow;
import ui.model.VarRow;

import java.util.List;

public class HistoryService {
    private ObservableList<HistoryRow> history =  FXCollections.observableArrayList();

    public ObservableList<HistoryRow> getHistory() {
        return history;
    }

    public void addHistory(long executeOutput, int degree, int cycles, List<VarRow> vars){
        String y = String.valueOf(executeOutput);
        String deg = String.valueOf(degree);
        String cyc = String.valueOf(cycles);
        String  runNumber = String.valueOf(history.size() + 1);
        HistoryRow newHistory = new HistoryRow(runNumber, y, deg, cyc, vars);
        history.add(newHistory);
    }

}
