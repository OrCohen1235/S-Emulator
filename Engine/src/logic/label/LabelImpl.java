package logic.label;

import java.util.Objects;
import java.util.Optional;

public final class LabelImpl implements Label {


    private final String label;

    public LabelImpl(int number) {
        this.label = Optional.of(number)
                .filter(n -> n > 0)
                .map(n -> "L" + n)
                .orElseThrow(() -> new IllegalArgumentException("The label number must be greater than 0 while it is " + number));

    }

    public LabelImpl(String token) {
        String t = Optional.ofNullable(token)
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("Label token is missing (received null).\n"));

        if (t.isEmpty() || t.equalsIgnoreCase("EMPTY")) {
            this.label = "";            // EMPTY
        } else if (t.equalsIgnoreCase("EXIT")) {
            this.label = "EXIT";        // EXIT
        } else if (t.matches("(?i)L\\d+")) {
            this.label = "L" + t.substring(1);
        } else {
            throw new IllegalArgumentException(
                    "The program should only have the following: EXIT, EMPTY, or L<number> While the label received is " + token + "\n");
        }
    }

    public int getIndexLabelNumber() {
        return Integer.parseInt(this.label.substring(1));
    }
    @Override
    public String getLabelRepresentation() {
        return label;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LabelImpl label1 = (LabelImpl) o;
        return Objects.equals(label, label1.label);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(label);
    }


}
