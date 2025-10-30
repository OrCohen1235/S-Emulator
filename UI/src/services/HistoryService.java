package services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.HistoryRow;
import model.VarRow;
import program.Program;

import java.util.List;

public class HistoryService {
    private ObservableList<HistoryRow> history = FXCollections.observableArrayList();

    public ObservableList<HistoryRow> getHistory() {
        return history;
    }

    public void addHistory(String currentProgramName, long executeOutput, int degree, int cycles, List<VarRow> vars) {
        String y = String.valueOf(executeOutput);
        String deg = String.valueOf(degree);
        String cyc = String.valueOf(cycles);
        String runNumber = String.valueOf(history.size());
    }
}






