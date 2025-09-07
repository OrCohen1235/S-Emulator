package ui.viewmodel;

import javafx.scene.control.TreeItem;
import logic.dto.InstructionDTO;
import ui.services.ProgramService;

import java.util.List;

public class InstructionsViewModel {
    private TreeItem<InstructionDTO> root;
    private int countB = 0, countS = 0, sumCycles = 0;

    public void reloadInstructions(ProgramService ps, int degree) {
        ps.loadExpasionByDegree(degree);
        var prog = ps.getProgram();
        var dtos = (prog != null) ? prog.getInstructionDTOs() : List.<InstructionDTO>of();

        countB = 0; countS = 0; sumCycles = 0;
        for (var dto : dtos) {
            sumCycles += Integer.parseInt(dto.getCycles());
            if ("B".equalsIgnoreCase(dto.getType())) countB++; else countS++;
        }
        root = new TreeItem<>(null);
        for (var dto : dtos) root.getChildren().add(new TreeItem<>(dto));
    }

    public TreeItem<InstructionDTO> getRoot() { return root; }

    public String buildSummary(ProgramService ps) {
        int size = (root == null) ? 0 : root.getChildren().size();
        return "Program loaded " + ps.getProgramName()
                + " | Instructions: " + size
                + " | B/S: " + countB + "/" + countS
                + " | Cycles: " + sumCycles;
    }
}
