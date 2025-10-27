package viewmodel;

import logic.instructions.Instruction;
import logic.instructions.InstructionData;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ארכיטקטורות של מעבד עם פעולות נתמכות
 */
public enum Architecture {
    I(5, Set.of(
            InstructionData.NEUTRAL,
            InstructionData.INCREASE,
            InstructionData.DECREASE,
            InstructionData.JUMP_NOT_ZERO
    )),

    II(100, Set.of(
            InstructionData.NEUTRAL,
            InstructionData.INCREASE,
            InstructionData.DECREASE,
            InstructionData.JUMP_NOT_ZERO,
            InstructionData.ZERO_VARIABLE,
            InstructionData.CONSTANT_ASSIGNMENT,
            InstructionData.GOTO_LABEL
    )),

    III(500, Set.of(
            InstructionData.NEUTRAL,
            InstructionData.INCREASE,
            InstructionData.DECREASE,
            InstructionData.JUMP_NOT_ZERO,
            InstructionData.ZERO_VARIABLE,
            InstructionData.CONSTANT_ASSIGNMENT,
            InstructionData.GOTO_LABEL,
            InstructionData.ASSIGNMENT,
            InstructionData.JUMP_ZERO,
            InstructionData.JUMP_EQUAL_CONSTANT,
            InstructionData.JUMP_EQUAL_VARIABLE
    )),

    IV(1000, Set.of(
            InstructionData.NEUTRAL,
            InstructionData.INCREASE,
            InstructionData.DECREASE,
            InstructionData.JUMP_NOT_ZERO,
            InstructionData.ZERO_VARIABLE,
            InstructionData.CONSTANT_ASSIGNMENT,
            InstructionData.GOTO_LABEL,
            InstructionData.ASSIGNMENT,
            InstructionData.JUMP_ZERO,
            InstructionData.JUMP_EQUAL_CONSTANT,
            InstructionData.JUMP_EQUAL_VARIABLE,
            InstructionData.QUOTE,
            InstructionData.JUMP_EQUAL_FUNCTION
    ));

    private final int creditsPerRun;
    private final Set<InstructionData> supportedOperations;

    Architecture(int creditsPerRun, Set<InstructionData> supportedOperations) {
        this.creditsPerRun = creditsPerRun;
        this.supportedOperations = supportedOperations;
    }

    public static Architecture parse(String architecture) {
        if (architecture == null || architecture.isBlank()) {
            return null;
        }

        try {
            return Architecture.valueOf(architecture.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            // אם הערך לא קיים ב-enum
            return null;
        }
    }


    public int getCreditsPerRun() {
        return creditsPerRun;
    }

    public boolean supports(InstructionData operation) {
        return supportedOperations.contains(operation);
    }

    /**
     * בדיקה אם הארכיטקטורה תומכת בשם פקודה (String)
     */
    public boolean supports(String instructionName) {
        try {
            InstructionData data = InstructionData.valueOf(instructionName);
            return supports(data);
        } catch (IllegalArgumentException e) {
            // אם השם לא קיים ב-InstructionData
            return false;
        }
    }

    @Override
    public String toString() {
        return name() + " (Credits: " + creditsPerRun + ", Operations: " + supportedOperations.size() + ")";
    }
}
