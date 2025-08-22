package program;

import logic.instructions.Instruction;
import logic.label.Label;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class ProgramView {

    public enum Mode {
        ORIGINAL
        , EXPANDED } // Two views: original instructions or expanded (flattened) list

    private final Supplier<List<Instruction>> originalSupplier; // Provides original list
    private final Supplier<List<Instruction>> expandedSupplier; // Provides expanded list

    private Mode mode = Mode.ORIGINAL; // Active mode

    public ProgramView(Supplier<List<Instruction>> originalSupplier, Supplier<List<Instruction>> expandedSupplier) {
        this.originalSupplier = (originalSupplier);
        this.expandedSupplier = (expandedSupplier);
    }

    public interface InstructionsView {
        List<Instruction> list();
        default Stream<Instruction> stream() { return list().stream(); } // Convenience stream
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

            return Optional.of(index)
                    .filter(i -> i >= 0 && i < list.size())
                    .map(list::get)
                    .orElseThrow(() -> new IndexOutOfBoundsException(
                            "Index " + index + " out of bounds for originalView (size=" + list.size() + ")\n"
                    )); // Bounds-checked access
        }

        public Instruction getInstructionByLabel(Label label) {
            Objects.requireNonNull(label, "Label must not be null\n");
            return originalSupplier.get().stream()
                    .filter(i -> label.equals(i.getLabel()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(
                            "No instruction found in originalView with label: " + label.getLabelRepresentation() + "\n"
                    )); // First instruction with matching label
        }

        public int getIndexByInstruction(Instruction inst) {
            Objects.requireNonNull(inst, "Instruction must not be null\n");
            return Optional.ofNullable(originalSupplier.get())
                    .map(list -> list.indexOf(inst))
                    .filter(i -> i != -1)
                    .orElseThrow(() -> new NoSuchElementException(
                            "Instruction not found in originalView: " + inst + "\n"
                    )); // Index lookup with error on missing
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
            Optional.of(index)
                    .filter(i -> i >= 0)
                    .orElseThrow(() -> new IndexOutOfBoundsException(
                            "Index " + index + " out of bounds for expandedView (size=" + list.size() + ")\n"
                    )); // Negative index guard

            return list.get(index); // Direct access (upper bound implicitly handled by List)
        }

        public Instruction getInstructionByLabel(Label label) {
            Objects.requireNonNull(label, "Label must not be null\n");
            return expandedSupplier.get().stream()
                    .filter(i -> label.equals(i.getLabel()))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchElementException(
                            "No instruction found in expandedView with label: " + label.getLabelRepresentation() + "\n"
                    )); // First instruction with matching label
        }

        public int getIndexByInstruction(Instruction inst) {
            Objects.requireNonNull(inst, "Instruction must not be null\n");
            int idx = expandedSupplier.get().indexOf(inst);
            if (idx == -1) {
                throw new NoSuchElementException("Instruction not found in expandedView: " + inst + "\n");
            }
            return idx; // Index in expanded list
        }

        public int getSizeOfListInstructions() {
            return expandedSupplier.get().size();
        }
    };

    public void useOriginal() { mode = Mode.ORIGINAL; } // Switch to original view
    public void useExpanded() { mode = Mode.EXPANDED; } // Switch to expanded view
    public Mode mode() { return mode; }                 // Current mode getter

    public InstructionsView active() {
        return (mode == Mode.ORIGINAL) ? originalView : expandedView; // Resolve active view
    }
}
