package program;

import logic.instructions.Instruction;
import logic.label.Label;

import java.util.List;
import java.util.NoSuchElementException;
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
        int getSizeOfListInstructions();
    }
    private final InstructionsView originalView = new InstructionsView() {
        public List<Instruction> list() {
            return originalSupplier.get();
        }

        public Instruction getInstructionByIndex(int index) {
            List<Instruction> list = originalSupplier.get();
            if (index < 0 || index >= list.size()) {
                throw new IndexOutOfBoundsException(
                        "Index " + index + " out of bounds for originalView (size=" + list.size() + ")\n"
                );
            }
            return list.get(index);
        }

        public Instruction getInstructionByLabel(Label label) {
            Objects.requireNonNull(label, "Label must not be null\n");
            return originalSupplier.get().stream()
                    .filter(i -> label.equals(i.getLabel()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(
                            "No instruction found in originalView with label: " + label.getLabelRepresentation() + "\n"
                    ));
        }

        public int getIndexByInstruction(Instruction inst) {
            Objects.requireNonNull(inst, "Instruction must not be null\n");
            int idx = originalSupplier.get().indexOf(inst);
            if (idx == -1) {
                throw new NoSuchElementException("Instruction not found in originalView: " + inst + "\n");
            }
            return idx;
        }

        public int getSizeOfListInstructions() {
            return originalSupplier.get().size();
        }
    };

    private final InstructionsView expandedView = new InstructionsView() {
        public List<Instruction> list() {
            return expandedSupplier.get();
        }

        public Instruction getInstructionByIndex(int index) {
            List<Instruction> list = expandedSupplier.get();
            if (index < 0) {
                throw new IndexOutOfBoundsException(
                        "Index " + index + " out of bounds for expandedView (size=" + list.size() + ")\n"
                );
            }
            return list.get(index);
        }

        public Instruction getInstructionByLabel(Label label) {
            Objects.requireNonNull(label, "Label must not be null\n");
            return expandedSupplier.get().stream()
                    .filter(i -> label.equals(i.getLabel()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(
                            "No instruction found in expandedView with label: " + label.getLabelRepresentation() + "\n"
                    ));
        }

        public int getIndexByInstruction(Instruction inst) {
            Objects.requireNonNull(inst, "Instruction must not be null\n");
            int idx = expandedSupplier.get().indexOf(inst);
            if (idx == -1) {
                throw new NoSuchElementException("Instruction not found in expandedView: " + inst + "\n");
            }
            return idx;
        }

        public int getSizeOfListInstructions() {
            return expandedSupplier.get().size();
        }
    };

    public void useOriginal() { mode = Mode.ORIGINAL; }
    public void useExpanded() { mode = Mode.EXPANDED; }
    public Mode mode() { return mode; }

    public InstructionsView active() {
        return (mode == Mode.ORIGINAL) ? originalView : expandedView;
    }
}
