package services;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.HistoryOldRow;
import model.HistoryRow;
import model.VarRow;
import program.Program;

import java.util.List;

public class HistoryService {
    private ObservableList<HistoryRow> history =  FXCollections.observableArrayList();

    public ObservableList<HistoryRow> getHistory() {
        return history;
    }

    public void setHistory(ObservableList<HistoryRow> history) {
        this.history = history;
    }

    public void addHistory(List<VarRow> vars){
        history.getLast().setAllRemainingHistory(vars);
    }

    public void createHistory(List<Long> statingInput) {
        HistoryRow newHistory = new HistoryRow(statingInput);
        this.history.add(newHistory);
    }






}