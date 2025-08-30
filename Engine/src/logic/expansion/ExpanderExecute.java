package logic.expansion;

import logic.instructions.Instruction;
import logic.instructions.sinstruction.SyntheticInstruction;
import program.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExpanderExecute {
    private final Program program;                     // Target program to expand
    private final ExpansionContext expansionContext;   // Supplies fresh labels/vars and config
    private final Expander expander;                   // Performs instruction-level expansion

    public ExpanderExecute(Program program) {
        this.program = program;
        expansionContext = new ExpansionContext(program, 1, getMaxLabelNumber() + 1); // Start degree=1; next free label
        expander = new Expander(expansionContext);
    }

    public void loadExpansion() {
        int maxLabel = expansionContext.getNextLabelIdx();;
        int maxWorkIndex= expansionContext.getNextWorkIdx();
        loadFullExpansion(program.getInstructions());
        expansionContext.setNextLabelIdx(maxLabel);
        expansionContext.setNextWorkIdx(maxWorkIndex);
        program.resetMapVariables();
        // Compute degrees for full expansion tree
    }

    private void loadFullExpansion(List<Instruction> listOfExpansion) {
        if (listOfExpansion.size() == 1) {
            return; // Leaf reached
        } else {
            for (Instruction instruction : listOfExpansion) {
                List<Instruction> lst = expander.expand(instruction); // Expand current node
                loadFullExpansion(lst); // Recurse
                if (instruction instanceof SyntheticInstruction) {
                    instruction.setDegree(calcMaxDegree(lst) + 1); // Degree = max(children)+1
                }
            }
        }
    }

    // -------------------- Expansion: by degree (build linear list) --------------------
    public void loadExpansionByDegree(int degree) {
        List<Instruction> out = new ArrayList<>();
        int maxLabel = expansionContext.getNextLabelIdx();;
        int maxWorkIndex= expansionContext.getNextWorkIdx();
        int fatherindex=1; // 1-based parent index in flattened view
        for (Instruction instruction : program.getInstructions()) {
            expandWithLimitedDegree(instruction, degree, out,fatherindex); // Depth-limited expansion
            fatherindex++;
        }
        expansionContext.setNextLabelIdx(maxLabel);
        expansionContext.setNextWorkIdx(maxWorkIndex);
        program.resetMapVariables();
        program.setExpandInstructionsByDegree(out); // Materialize chosen-degree view
    }

    private void expandWithLimitedDegree(Instruction instruction, int remaining, List<Instruction> out,int fatherindex) {
        boolean isSynthetic = instruction instanceof SyntheticInstruction;

        if (remaining == 0 || !isSynthetic) {
            out.add(instruction); // Stop expanding: either depth reached or primitive
            return;
        }
        List<Instruction> children = expander.expand(instruction); // Expand one level
        for (Instruction child : children) {
            child.setFather(instruction); // Track parent linkage
            expandWithLimitedDegree(child, remaining - 1, out,fatherindex+1); // Recurse with reduced budget
            child.setIndexFatherLocation(fatherindex); // Store parent index for pretty-print
        }

        if (isSynthetic) {
            int maxChildDegree = 0;
            for (Instruction child : children) {
                maxChildDegree = Math.max(maxChildDegree, child.getDegree());
            }
            instruction.setDegree(maxChildDegree + 1); // Update parent degree from children
        }
    }

    // -------------------- Expansion: queries (max degree, label max, pretty print) --------------------

    public int getMaxDegree() {
        int max;
        if (program.getMaxDegree() == -1) {
            max = calcMaxDegree(program.getInstructions());
            program.setMaxDegree(max);
        }
        else {
            max=program.getMaxDegree();
        }
        program.resetMapVariables();
        return max;
    }

    private int calcMaxDegree(List<Instruction> instList) {
        int maxDegree = 0;
        for (Instruction inst : instList) {
            if (maxDegree < inst.getDegree()) {
                maxDegree = inst.getDegree();
            }
        }
        return maxDegree; // Returns 0 if degrees were not set
    }

    public int getMaxLabelNumber() {
        return program.getLabels().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(lbl -> !"EXIT".equalsIgnoreCase(lbl))
                .filter(lbl -> lbl.startsWith("L")
                        && lbl.length() > 1
                        && lbl.substring(1).chars().allMatch(Character::isDigit))
                .mapToInt(lbl -> Integer.parseInt(lbl.substring(1)))
                .max()
                .orElse(0); // Highest numeric L* label or 0 if none
    }
}
