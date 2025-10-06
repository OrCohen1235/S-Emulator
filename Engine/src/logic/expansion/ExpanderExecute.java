package logic.expansion;

import logic.function.Function;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.binstruction.*;
import logic.instructions.sinstruction.*;
import logic.label.Label;
import logic.label.LabelImpl;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import program.Program;
import program.ProgramLoadException;

import java.util.*;

public class ExpanderExecute {
    private final Program program;
    private final ExpansionContext expansionContext;   // Supplies fresh labels/vars and config
    private final Expander expander;                   // Performs instruction-level expansion

    public ExpanderExecute(Program program) {
        this.program = program;
        if (!program.getIsMainProgram()) {
            expansionContext = new ExpansionContext(program, program.getMaxWorkIndex()+1, getMaxLabelNumber() + 1);
        }// Start degree=1; next free label
        else {
            expansionContext = new ExpansionContext(program, program.getMaxWorkIndex()+1, getMaxLabelNumber() + 1);
        }
        expander = new Expander(expansionContext);
    }

    public void loadExpansion() {
        int maxLabel = expansionContext.getNextLabelIdx();;
        int maxWorkIndex= expansionContext.getNextWorkIdx();
        loadFullExpansion(program.getActiveInstructions());
        expansionContext.setNextLabelIdx(maxLabel);
        expansionContext.setNextWorkIdx(maxWorkIndex);
        program.resetMapVariables();
        program.resetFunctions();
        // Compute degrees for full expansion tree
    }

    private void loadFullExpansion(List<Instruction> listOfExpansion) {
        if (listOfExpansion.size() == 1 && listOfExpansion.get(0) instanceof BaseInstruction) {
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
        expander.setCurrDeg(degree);
        List<Instruction> out = new ArrayList<>();
        int maxLabel = expansionContext.getNextLabelIdx();;
        int maxWorkIndex= expansionContext.getNextWorkIdx();
        int expand=0; // 1-based parent index in flattened view

        for (int i=0; i<=degree; i++) {

            if (i==0)
            {
                program.setExpandInstructionsByDegreeHelper(program.getOriginalInstructions());
            }
            for (Instruction instruction : program.getExpandInstructionsByDegreeHelper()) {
                expandWithLimitedDegree(instruction, expand, out);
            }
            if (i+1==1){
                expand=1;
            }
            for (Instruction instruction : out) {
                int index=0;
                if (instruction.getFather()!=null) {
                    index = program.getIndexHelper(instruction.getFather());
                    if (index == 0)
                    {
                        index = instruction.getIndexFatherLocation();
                    }
                }
                instruction.setIndexFatherLocation(index);
            }
            program.setExpandInstructionsByDegreeHelper(out);
            if (i<degree) {
                out.clear();
            }

        }


        expansionContext.setNextLabelIdx(maxLabel);
        expansionContext.setNextWorkIdx(maxWorkIndex);
        program.setExpandInstructionsByDegree(out);
        program.resetMapVariables();
        program.resetFunctions();
        out.clear();
        program.setExpandInstructionsByDegreeHelper(out);
        // Materialize chosen-degree view
    }


    private void expandWithLimitedDegree(Instruction instruction, int remaining, List<Instruction> out) {
        boolean isSynthetic = instruction instanceof SyntheticInstruction;


        if (remaining == 0 || !isSynthetic) {
            out.add(instruction); // Stop expanding: either depth reached or primitive
            return;
        }
        List<Instruction> children = expander.expand(instruction); // Expand one level
        for (Instruction child : children) {
            child.setFather(instruction); // Track parent linkage
            expandWithLimitedDegree(child, remaining - 1, out);
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
            max = calcMaxDegree(program.getActiveInstructions());
            program.setMaxDegree(max);
        }
        else {
            max=program.getMaxDegree();
        }
        program.resetMapVariables();
        program.resetFunctions();
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

    public Variable getFreshWork(){
        return expansionContext.getFreshWorkVal();
    }

    public void setMaxWorkIndex(int maxWorkIndex) {
        expansionContext.setNextWorkIdx(maxWorkIndex);
    }


}
