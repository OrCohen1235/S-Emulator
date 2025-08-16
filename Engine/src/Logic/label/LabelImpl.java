package Logic.label;

import java.util.Objects;
import java.util.Optional;

public final class LabelImpl implements Label {

    private final String label;

    public LabelImpl(int number) {
        this.label = Optional.of(number)
                .filter(n -> n > 0)
                .map(n -> "L" + n)
                .orElseThrow(() -> new IllegalArgumentException("number must be > 0"));

    }

    public LabelImpl(String token) {
        String t = Optional.ofNullable(token)
                .map(String::trim)
                .orElseThrow(() -> new IllegalArgumentException("token is null"));

        if (t.isEmpty() || t.equalsIgnoreCase("EMPTY")) {
            this.label = "";            // EMPTY
        } else if (t.equalsIgnoreCase("EXIT")) {
            this.label = "EXIT";        // EXIT
        } else if (t.matches("(?i)L\\d+")) {
            this.label = "L" + t.substring(1);
        } else {
            throw new IllegalArgumentException(
                    "Expected EXIT, EMPTY, or L<number>. Got: " + token);
        }
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
