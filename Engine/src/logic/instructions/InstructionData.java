package logic.instructions;

import java.util.*;
import java.util.stream.Collectors;

public enum InstructionData {
    INCREASE("INCREASE", 1),
    DECREASE("DECREASE", 1),
    NEUTRAL("NEUTRAL", 0),
    JUMP_NOT_ZERO("JUMP_NOT_ZERO", 2),
    ZERO_VARIABLE ("ZERO_VARIABLE", 1),
    GOTO_LABEL ("GOTO_LABEL", 1),
    ASSIGNMENT ("ASSIGNMENT", 4),
    CONSTANT_ASSIGNMENT ("CONSTANT_ASSIGNMENT", 2),
    JUMP_ZERO ("JUMP_ZERO", 2),
    JUMP_EQUAL_CONSTANT ("JUMP_EQUAL_CONSTANT", 2),
    JUMP_EQUAL_VARIABLE ("JUMP_EQUAL_VARIABLE", 2);

    private final String name;
    private final int cycles;

    InstructionData(String name, int cycles) {
        this.name = name;
        this.cycles = cycles;
    }
    public String getName()   { return name; }
    public int getCycles()    { return cycles; }

    private static final Map<String, InstructionData> BY_NAME =
            Arrays.stream(values())
                    .collect(Collectors.toMap(v -> v.name, v -> v));

    public static InstructionData fromName(String s) {
        if (s == null) throw new IllegalArgumentException("The program name does not exist in the xml file.\n");
        String key = s.trim();
        InstructionData v = BY_NAME.get(key);
        if (v == null) {
            v = BY_NAME.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(key))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        if (v == null) throw new IllegalArgumentException("Unknown instruction: " + s + "\n");
        return v;
    }
}
