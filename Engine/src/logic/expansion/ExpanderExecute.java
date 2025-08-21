package logic.expansion;

import logic.instructions.Instruction;
import logic.instructions.sinstruction.SyntheticInstruction;
import program.Program;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ExpanderExecute {
    private Program program;
    private ExpansionContext expansionContext;
    private Expander expander;

    public ExpanderExecute(Program program) {
        this.program = program;
        expansionContext = new ExpansionContext(program, 1, getMaxLabelNumber() + 1);
        expander = new Expander(expansionContext);
    }

    public void loadExpansion() {
        loadFullExpansion(program.getInstructions());
    }

    private void loadFullExpansion(List<Instruction> listOfExpansion) {
        if (listOfExpansion.size() == 1) {
            return;
        } else {
            for (Instruction instruction : listOfExpansion) {
                List<Instruction> lst = expander.expand(instruction);
                loadFullExpansion(lst);
                if (instruction instanceof SyntheticInstruction) {
                    instruction.setDegree(calcMaxDegree(lst) + 1);
                }
            }
        }
    }

    // -------------------- Expansion: by degree (build linear list) --------------------
    public void loadExpansionByDegree(int degree) {
        List<Instruction> out = new ArrayList<>();
        int fatherindex=1;
        for (Instruction instruction : program.getInstructions()) {
            expandWithLimitedDegree(instruction, degree, out,fatherindex);
            fatherindex++;
        }
        program.setExpandInstructionsByDegree(out);
    }

    private void expandWithLimitedDegree(Instruction instruction, int remaining, List<Instruction> out,int fatherindex) {
        boolean isSynthetic = instruction instanceof SyntheticInstruction;

        if (remaining == 0 || !isSynthetic) {
            out.add(instruction);
            return;
        }
        List<Instruction> children = expander.expand(instruction);
        for (Instruction child : children) {
            child.setFather(instruction);
            expandWithLimitedDegree(child, remaining - 1, out,fatherindex+1);
            child.setIndexFatherLocation(fatherindex);
        }

        if (isSynthetic) {
            int maxChildDegree = 0;
            for (Instruction child : children) {
                maxChildDegree = Math.max(maxChildDegree, child.getDegree());
            }
            instruction.setDegree(maxChildDegree + 1);
        }
    }

    // -------------------- Expansion: queries (max degree, label max, pretty print) --------------------

    public int getMaxDegree() {
        int max = calcMaxDegree(program.getInstructions());
        program.setMaxDegree(max);
        return max;
    }

    private int calcMaxDegree(List<Instruction> instList) {
        int maxDegree = 0;
        for (Instruction inst : instList) {
            if (maxDegree < inst.getDegree()) {
                maxDegree = inst.getDegree();
            }
        }
        return maxDegree;
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
                .orElse(0);
    }
}
