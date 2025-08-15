package Logic;

import java.util.*;
import java.util.stream.Collectors;

public enum InstructionData {
    INCREASE("INCREASE", 1),
    DECREASE("DECREASE", 1),
    Neutral("Neutral", 0),
    JUMP_NOT_ZERO("JUMP_NOT_ZERO", 3);

    private final String name;
    private final int cycles;

    InstructionData(String name, int cycles) {
        this.name = name;
        this.cycles = cycles;
    }
    public String getName()   { return name; }
    public int getCycles()    { return cycles; }

    // --- מיפוי יעיל מ-name -> enum ---
    private static final Map<String, InstructionData> BY_NAME =
            Arrays.stream(values())
                    .collect(Collectors.toMap(v -> v.name, v -> v));

    public static InstructionData fromName(String s) {
        if (s == null) throw new IllegalArgumentException("name is null");
        String key = s.trim();
        InstructionData v = BY_NAME.get(key);
        if (v == null) {
            // נסה גם case-insensitive
            v = BY_NAME.entrySet().stream()
                    .filter(e -> e.getKey().equalsIgnoreCase(key))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
        }
        if (v == null) throw new IllegalArgumentException("Unknown instruction: " + s);
        return v;
    }
}
