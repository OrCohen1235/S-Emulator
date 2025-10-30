package users;

import model.HistoryRow;

import java.util.ArrayList;
import java.util.List;

/**
 * מייצג משתמש במערכת - הסטטיסטיקות שלו
 */
public class User {

    private final String username;

    // סטטיסטיקות
    private int creditsCurrent = 0;
    private int creditsUsed = 0;
    private int mainProgramsUploaded = 0;
    private int functionsContributed = 0;
    private int runsCount = 0;
    private List<HistoryRow> historyRowList = new ArrayList<>();

    public User(String username) {
        this.username = username;
    }

    // ========== Getters ==========

    public String getUsername() {
        return username;
    }

    public int getCreditsCurrent() {
        return creditsCurrent;
    }

    public int getCreditsUsed() {
        return creditsUsed;
    }

    public int getMainProgramsUploaded() {
        return mainProgramsUploaded;
    }

    public int getFunctionsContributed() {
        return functionsContributed;
    }

    public int getRunsCount() {
        return runsCount;
    }

    public List<HistoryRow> getHistoryRowList() {
        return historyRowList;
    }

    public synchronized void addHistoryRow(HistoryRow historyRow) {
        historyRowList.add(historyRow);
    }

    // ========== Actions ==========

    /**
     * הוספת קרדיטים
     */
    public synchronized void addCredits(int amount) {
        if (amount > 0) {
            creditsCurrent += amount;
        }
    }

    /**
     * שימוש בקרדיטים
     * @return true אם הצליח, false אם אין מספיק
     */
    public synchronized boolean useCredits(int amount,int architecture) {
        if (amount+architecture > creditsCurrent) {
            creditsUsed += creditsCurrent +architecture;
            return false;
        }
        creditsUsed += amount;
        return true;
    }

    /**
     * הוספת ספירת הרצות
     */
    public synchronized void incrementRunsCount() {
        runsCount++;
    }

    /**
     * הוספת תוכנית ראשית
     */
    public synchronized void addMainProgram() {
        mainProgramsUploaded++;
    }

    /**
     * הוספת תרומת פונקציה
     */
    public synchronized void addFunctionContribution(int functionsContributed) {
        this.functionsContributed += functionsContributed;
    }

    @Override
    public String toString() {
        return String.format(
                "User{username='%s', credits=%d, used=%d, programs=%d, runs=%d}",
                username, creditsCurrent, creditsUsed, mainProgramsUploaded, runsCount
        );
    }

    public synchronized void decreaseCredits(int amount) {
        if (creditsCurrent - amount < 0){
            creditsCurrent = 0;
            return;
        }
        creditsCurrent -= amount;
    }

    public void setCreditsCurrent(int credits) {
        this.creditsCurrent = credits;
    }
}