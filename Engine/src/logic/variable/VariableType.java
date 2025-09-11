package logic.variable;

import java.util.Comparator;

public enum VariableType {
    RESULT {
        @Override
        public String getVariableRepresentation(int number) {
            return "Y";
        }
    },
    INPUT {
        @Override
        public String getVariableRepresentation(int number) {
            return "X" + number;
        }
    },
    WORK {
        @Override
        public String getVariableRepresentation(int number) {
            return "Z" + number;
        }
    };


    public abstract String getVariableRepresentation(int number);

    public static VariableType fromToken(String token) {
        if (token == null) throw new IllegalArgumentException("token is null");
        String t = token.trim();
        if (!t.matches("[xX]\\d+|[yY]|[zZ]\\d+"))
            throw new IllegalArgumentException("Bad format. Expected xi, y, or zi. Got: " + token);

        char c = Character.toLowerCase(t.charAt(0));
        return switch (c) {
            case 'x' -> INPUT;
            case 'y' -> RESULT;
            case 'z' -> WORK;
            default  -> throw new IllegalStateException("Unexpected variable type: " + c + "\n");
        };
    }


}