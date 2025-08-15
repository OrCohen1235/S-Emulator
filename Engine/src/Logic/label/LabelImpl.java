package Logic.label;

public final class LabelImpl implements Label {

    private final String label;

    public LabelImpl(int number) {
        if (number < 0) throw new IllegalArgumentException("number must be >= 0");
        this.label = "L" + number;
    }

    public LabelImpl(String token) {
        if (token == null) throw new IllegalArgumentException("token is null");
        String t = token.trim();

        if (t.isEmpty() || t.equalsIgnoreCase("EMPTY")) {
            this.label = "";            // EMPTY
        } else if (t.equalsIgnoreCase("EXIT")) {
            this.label = "EXIT";        // EXIT
        } else if (t.matches("(?i)L\\d+")) {
            this.label = "L" + t.substring(1); // מנרמל ל-L גדולה
        } else {
            throw new IllegalArgumentException(
                    "Expected EXIT, EMPTY, or L<number>. Got: " + token);
        }
    }



    @Override
    public String getLabelRepresentation() {
        return label;
    }

}
