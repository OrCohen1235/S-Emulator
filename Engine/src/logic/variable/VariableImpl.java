package logic.variable;

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
                .orElseThrow(() -> new IllegalArgumentException("Variable name is null or empty. \n"));


        char prefix = Character.toLowerCase(t.charAt(0));
        switch (prefix) {
            case 'y' -> {
                if (t.length() != 1) {
                    throw new IllegalArgumentException("RESULT ('y') must not have an index while received: " + name + "\n");
                }
                this.type = VariableType.RESULT;
                this.number = 0;
            }
            case 'x' -> {
                this.type = VariableType.INPUT;
                this.number = parseIndex(t, 1);
            }
            case 'z' -> {
                this.type = VariableType.WORK;
                this.number = parseIndex(t, 1);
            }
            default -> throw new IllegalArgumentException(
                    "Unknown variable prefix '" + t.charAt(0) + "'. Expected x, y, or z. Got: " + name + "\n");
        }
    }

    private int parseIndex(String s, int from) {
        String numberPart = Optional.ofNullable(s)
                .filter(str -> from >= 0 && from < str.length())
                .map(str -> str.substring(from))
                .filter(part -> part.chars().allMatch(Character::isDigit))
                .orElseThrow(() -> new IllegalArgumentException("Missing or non-numeric index in: " + s + "\n"));

        return Optional.of(Integer.parseInt(numberPart))
                .filter(i -> i >= 0)
                .orElseThrow(() -> new IllegalArgumentException(
                        "The variable number must be a positive number while the variable number received is: "
                                + numberPart + "\n"));
    }

    @Override
    public VariableType getType() {
        return type;
    }

    @Override
    public int getVariableIndex() { return number; }

    @Override
    public String getRepresentation() {
        return type.getVariableRepresentation(number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Variable)) return false;
        Variable other = (Variable) o;
        return this.number == other.getVariableIndex()
                && this.type == other.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, number);
    }
}
