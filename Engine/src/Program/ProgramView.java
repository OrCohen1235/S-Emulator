package Program;

import Logic.Instructions.Instruction;
import Logic.label.Label;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ProgramView {

    public enum Mode {
        ORIGINAL
        , EXPANDED }

    private final Supplier<List<Instruction>> originalSupplier;
    private final Supplier<List<Instruction>> expandedSupplier;

    private Mode mode = Mode.ORIGINAL;

    public ProgramView(Supplier<List<Instruction>> originalSupplier,
                        Supplier<List<Instruction>> expandedSupplier) {
        this.originalSupplier = (originalSupplier);
        this.expandedSupplier = (expandedSupplier);
    }

    public interface InstructionsView {
        List<Instruction> list();
        default Stream<Instruction> stream() { return list().stream(); }
        Instruction getInstructionByIndex(int index);
        Instruction getInstructionByLabel(Label label);
        int getIndexByInstruction(Instruction inst);
    }

    private final InstructionsView originalView = new InstructionsView() {
        public List<Instruction> list() { return originalSupplier.get(); }
        public Instruction getInstructionByIndex(int index) { return originalSupplier.get().get(index); }
        public Instruction getInstructionByLabel(Label label) {
            return originalSupplier.get().stream()
                    .filter(i -> label.equals(i.getLabel()))
                    .findFirst().orElse(null);
        }
        public int getIndexByInstruction(Instruction inst) { return originalSupplier.get().indexOf(inst); }
    };

    private final InstructionsView expandedView = new InstructionsView() {
        public List<Instruction> list() { return expandedSupplier.get(); }
        public Instruction getInstructionByIndex(int index) { return expandedSupplier.get().get(index); }
        public Instruction getInstructionByLabel(Label label) {
            return expandedSupplier.get().stream()
                    .filter(i -> label.equals(i.getLabel()))
                    .findFirst().orElse(null);
        }
        public int getIndexByInstruction(Instruction inst) { return expandedSupplier.get().indexOf(inst); }
    };

    public void useOriginal() { mode = Mode.ORIGINAL; }
    public void useExpanded() { mode = Mode.EXPANDED; }
    public Mode mode() { return mode; }

    public InstructionsView active() {
        return (mode == Mode.ORIGINAL) ? originalView : expandedView;
    }
}
