package Logic.variable;

import java.util.Objects;

public class VariableImpl implements Variable {

    private final VariableType type;
    private final int number;

    public VariableImpl(VariableType type, int number) {
        this.type = type;
        this.number = number;
    }

    public VariableImpl(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        String t = name.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("name is empty");
        }

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
        if (s.length() <= from) {
            throw new IllegalArgumentException("Missing numeric index in: " + s);
        }
        for (int i = from; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                throw new IllegalArgumentException("Index must be numeric. Got: " + s.substring(from) + " in " + s);
            }
        }
        try {
            int idx = Integer.parseInt(s.substring(from));
            if (idx < 0) {
                throw new IllegalArgumentException("Index must be non-negative. Got: " + idx);
            }
            return idx;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric index in: " + s, e);
        }
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
