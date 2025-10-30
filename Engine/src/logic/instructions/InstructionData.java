package logic.instructions;

import java.util.*;
import java.util.stream.Collectors;

public enum InstructionData {
    INCREASE("INCREASE", 1, "I"),
    DECREASE("DECREASE", 1, "I"),
    NEUTRAL("NEUTRAL", 0, "I"),
    JUMP_NOT_ZERO("JUMP_NOT_ZERO", 2, "I"),
    ZERO_VARIABLE ("ZERO_VARIABLE", 1, "II"),
    GOTO_LABEL ("GOTO_LABEL", 1, "II"),
    ASSIGNMENT ("ASSIGNMENT", 4, "III"),
    CONSTANT_ASSIGNMENT ("CONSTANT_ASSIGNMENT", 2, "II"),
    JUMP_ZERO ("JUMP_ZERO", 2, "III"),
    JUMP_EQUAL_CONSTANT ("JUMP_EQUAL_CONSTANT", 2, "III"),
    JUMP_EQUAL_VARIABLE ("JUMP_EQUAL_VARIABLE",2, "III"),
    QUOTE("QUOTE",5, "IV"),
    JUMP_EQUAL_FUNCTION ("JUMP_EQUAL_FUNCTION", 6, "IV");

    private final String name;
    private final int cycles;
    private final String architecture;

    InstructionData(String name, int cycles, String architecture) {
        this.name = name;
        this.cycles = cycles;
        this.architecture = architecture;
    }

    public String getName()   { return name; }
    public int getCycles()    { return cycles; }
    public String getArchitecture() { return architecture; }

    private static final Map<String, InstructionData> BY_NAME =
            Arrays.stream(values())
                    .collect(Collectors.toMap(v -> v.name, v -> v));

    public static InstructionData fromName(String name) {
        Optional.ofNullable(name)
                .orElseThrow(() -> new IllegalArgumentException(
                        "The program name does not exist in the xml file.\n"));

        String key = name.trim();

        InstructionData v = Optional.ofNullable(BY_NAME.get(key))
                .or(() -> BY_NAME.entrySet().stream()
                        .filter(e -> e.getKey().equalsIgnoreCase(key))
                        .map(Map.Entry::getValue)
                        .findFirst())
                .orElse(null);

        return Optional.ofNullable(v)
                .orElseThrow(() -> new IllegalArgumentException("Unknown instruction: " + name + "\n"));
    }



}