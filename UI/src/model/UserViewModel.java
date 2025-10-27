package model;

import javafx.beans.property.*;

public class UserViewModel {
    private final StringProperty name = new SimpleStringProperty();
    private final IntegerProperty mainPrograms = new SimpleIntegerProperty();
    private final IntegerProperty functions = new SimpleIntegerProperty();
    private final IntegerProperty creditsCurrent = new SimpleIntegerProperty();
    private final IntegerProperty creditsUsed = new SimpleIntegerProperty();
    private final IntegerProperty runs = new SimpleIntegerProperty();

    public UserViewModel(String name, int mainPrograms, int functions,
                     int creditsCurrent, int creditsUsed, int runs) {
        this.name.set(name);
        this.mainPrograms.set(mainPrograms);
        this.functions.set(functions);
        this.creditsCurrent.set(creditsCurrent);
        this.creditsUsed.set(creditsUsed);
        this.runs.set(runs);
    }

    // properties (לטבלת ה־FXML)
    public StringProperty nameProperty() { return name; }
    public IntegerProperty mainProgramsProperty() { return mainPrograms; }
    public IntegerProperty functionsProperty() { return functions; }
    public IntegerProperty creditsCurrentProperty() { return creditsCurrent; }
    public IntegerProperty creditsUsedProperty() { return creditsUsed; }
    public IntegerProperty runsProperty() { return runs; }

    // getters (נוח לשימוש)
    public String getName() { return name.get(); }
    public int getMainPrograms() { return mainPrograms.get(); }
    public int getFunctions() { return functions.get(); }
    public int getCreditsCurrent() { return creditsCurrent.get(); }
    public int getCreditsUsed() { return creditsUsed.get(); }
    public int getRuns() { return runs.get(); }


    public void updateFrom(UserViewModel other) {
        mainPrograms.set(other.getMainPrograms());
        functions.set(other.getFunctions());
        creditsCurrent.set(other.getCreditsCurrent());
        creditsUsed.set(other.getCreditsUsed());
        runs.set(other.getRuns());
    }

    public void increaseRuns(){
        runs.set(runs.get() + 1);
    }

}
