package Logic.variable;

import java.util.Objects;
import java.util.Optional;

public class VariableImpl implements Variable {

    private final VariableType type;
    private final int number;

    public VariableImpl(VariableType type, int number) {
        this.type = type;
        this.number = number;
    }

    public VariableImpl(String name) {
        String t = Optional.ofNullable(name)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("name is null or empty"));


        char prefix = Character.toLowerCase(t.charAt(0));
        switch (prefix) {
            case 'y' -> {
                if (t.length() != 1) {
                    throw new IllegalArgumentException("RESULT ('y') must not have an index: " + name);
                }
                this.type = VariableType.RESULT;
                this.number = 0;
            }
            case 'x' -> {
                this.type = VariableType.INPUT;
                this.number = parseIndex(t, 1); // אחרי האות
            }
            case 'z' -> {
                this.type = VariableType.WORK;
                this.number = parseIndex(t, 1); // אחרי האות
            }
            default -> throw new IllegalArgumentException(
                    "Unknown variable prefix '" + t.charAt(0) + "'. Expected x, y, or z. Got: " + name);
        }
    }

    private int parseIndex(String s, int from) {
        String numberPart = Optional.ofNullable(s)
                .filter(str -> from >= 0 && from < str.length())
                .map(str -> str.substring(from))
                .filter(part -> part.chars().allMatch(Character::isDigit))
                .orElseThrow(() -> new IllegalArgumentException("Missing or non-numeric index in: " + s));

        int idx = Integer.parseInt(numberPart);
        if (idx < 0) {
            throw new IllegalArgumentException("Index must be non-negative. Got: " + idx);
        }
        return idx;
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(number);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VariableImpl variable = (VariableImpl) o;
        return number == variable.number && type == variable.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }
}
