package users;

/**
 * מייצג משתמש אחד במערכת.
 */
public class User {
    private final String username;
    private int mainProgramsUploaded = 0;
    private int functionsContributed = 0;
    private int creditsCurrent = 0;
    private int creditsUsed = 0;
    private int runsCount = 0;

    public User(String username) {
        this.username = username;
    }

    // --- Getters ---
    public String getUsername() { return username; }
    public int getMainProgramsUploaded() { return mainProgramsUploaded; }
    public int getFunctionsContributed() { return functionsContributed; }
    public int getCreditsCurrent() { return creditsCurrent; }
    public int getCreditsUsed() { return creditsUsed; }
    public int getRunsCount() { return runsCount; }

    // --- Actions / Updates ---
    public void addMainProgram() {
        mainProgramsUploaded++;
    }

    public void addFunctionContribution() {
        functionsContributed++;
    }

    public void addCredits(int amount) {
        creditsCurrent += amount;
    }

    public boolean useCredits(int amount) {
        if (amount <= 0 || amount > creditsCurrent) {
            return false;
        }
        creditsCurrent -= amount;
        creditsUsed += amount;
        return true;
    }

    public void incrementRunsCount() {
        runsCount++;
    }

    // --- Utility ---
    @Override
    public String toString() {
        return String.format(
                "User{name='%s', mainPrograms=%d, functions=%d, credits=%d, used=%d, runs=%d}",
                username, mainProgramsUploaded, functionsContributed, creditsCurrent, creditsUsed, runsCount
        );
    }

    public void setCreditsCurrent(int credits) {
        this.creditsCurrent = credits;
    }

    public void setMainProgramsUploaded(int mainProgramsUploaded) {
        this.mainProgramsUploaded = mainProgramsUploaded;
    }

    public void setFunctionsContributed(int functionsContributed) {
        this.functionsContributed = functionsContributed;
    }

    public void setCreditsUsed(int creditsUsed) {
        this.creditsUsed = creditsUsed;
    }

    public void setRunsCount(int runsCount) {
        this.runsCount = runsCount;
    }
}
