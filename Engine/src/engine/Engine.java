package engine;

import Logic.DTO.ProgramDTO;
import Logic.Instructions.Instruction;
import Logic.Instructions.SInstruction.SyntheticInstruction;
import Logic.Program;
import Logic.execution.ProgramExecutorImpl;
import Logic.expansion.Expander;
import Logic.expansion.ExpansionContext;
import semulator.ReadSemulatorXml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Engine {

    // -------------------- Fields --------------------
    private ReadSemulatorXml readSem;
    private final Program program;
    private final ProgramDTO programDTO;
    private Boolean isLoaded = false;
    private final ProgramExecutorImpl programExecutor;
    private ExpansionContext expansionContext;
    private Expander expander;

    // -------------------- Constructor --------------------
    public Engine(File file) {
        try {
            readSem = new ReadSemulatorXml(file);
            isLoaded = true;
        } catch (Exception e) {
            // intentionally swallowed per original logic
        }

        program = new Program();
        program.loadProgram(readSem);

        programDTO = new ProgramDTO(program);
        programExecutor = new ProgramExecutorImpl(program);

        // uses programDTO labels to compute next fresh label index
        expansionContext = new ExpansionContext(program, 1, getMaxLabelNumber() + 1);
        expander = new Expander(expansionContext);
    }

    // -------------------- Basic accessors --------------------
    public Boolean getLoaded() { return isLoaded; }
    public void setLoaded(Boolean isLoaded) { this.isLoaded = isLoaded; }
    public ProgramDTO getProgramDTO() { return programDTO; }

    // -------------------- Run / Inputs --------------------
    public Long runProgramExecutor(int degree) {
        if (degree == 0) {
            return programExecutor.run();
        } else {
            return programExecutor.runByDegree();
        }
    }

    public void loadInputVars(List<Long> input) {
        program.loadInputVars(input);
    }

    // -------------------- Expansion: compute degrees (full) --------------------
    /** Computes expansion tree and degrees for all instructions (no list is returned). */
    public void loadExpansion() {
        loadFullExpansion(program.getInstrutions());
    }

    private void loadFullExpansion(List<Instruction> listOfExpansion) {
        if (listOfExpansion.size() == 1) {
            // no-op (per original logic)
        } else {
            for (Instruction instruction : listOfExpansion) {
                List<Instruction> lst = expander.expand(instruction);
                for (Instruction expadInstruction : lst) {
                    expadInstruction.setFather(instruction);
                }
                loadFullExpansion(lst);
                if (instruction instanceof SyntheticInstruction) {
                    instruction.setDegree(getMaxDegreeRecursive(lst) + 1);
                }
            }
        }
    }

    // -------------------- Expansion: by degree (build linear list) --------------------
    public List<Instruction> loadExpansionByDegree(int degree) {
        int target = Math.max(0, degree); // why do we need target? why dont we use dgree?
        List<Instruction> out = new ArrayList<>();
        for (Instruction instruction : program.getInstrutions()) {
            expandLimited(instruction, target, out);
        }
        program.setExpandInstructionsByDegree(out);
        return out;
    }

    private void expandLimited(Instruction instruction, int remaining, List<Instruction> out) {
        boolean isSynthetic = instruction instanceof SyntheticInstruction;

        if (remaining == 0 || !isSynthetic) {
            out.add(instruction);
            return;
        }

        List<Instruction> children = expander.expand(instruction);
        for (Instruction child : children) {
            child.setFather(instruction);
            expandLimited(child, remaining - 1, out);
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

    //we dont use getMaxDegree() and we have getMaxDegreeRecursive(), do we need both of them?
    public int getMaxDegree() {
        int max = getMaxDegreeRecursive(program.getInstrutions());
        program.setMaxDegree(max);
        return max;
    }

    private int getMaxDegreeRecursive(List<Instruction> instList) {
        int maxDegree = 0;
        for (Instruction inst : instList) {
            if (maxDegree < inst.getDegree()) {
                maxDegree = inst.getDegree();
            }
        }
        return maxDegree;
    }

    public int getMaxLabelNumber() {
        return programDTO.getLabels().stream()
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

    // no usages to this function, we need her?
    public List<String> getListOfExpandCommands(int degree) {
        List<String> prints = new ArrayList<>();
        List<Instruction> flattened = loadExpansionByDegree(degree);
        int index = 0;
        for (Instruction i : flattened) {
            if (degree != 0) {
                prints.add(programDTO.getSingleCommandAndFather(index, i));
            } else {
                prints.add(programDTO.getSingleCommand(index, i));
            }
            index++;
        }
        return prints;
    }

    // -------------------- Cycles --------------------
    // no usages to this function, we need her?
    public int getSumOfCycles() {
        return programExecutor.getSumOfCycles();
    }

    /** Better name; keeps logic identical. */
    public void resetSumOfCycles() {
        programExecutor.setSumOfCycles(0);
    }

    // ---- Backwards-compat wrappers (do not change logic) ----
    /** Legacy name kept for compatibility. */
    @Deprecated
    public void ResetSumOfCycles() { resetSumOfCycles(); }

    /** Alias used elsewhere (e.g., Menu). */
    public void setSumOfCycles() { resetSumOfCycles(); }
}
