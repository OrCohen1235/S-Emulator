package viewmodel;

import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import logic.dto.InstructionDTO;
import services.ProgramService;

import java.util.List;

public class InstructionsViewModel {
    private TreeItem<InstructionDTO> root;
    private TreeItem<InstructionDTO> expandroot;
    private int countB = 0, countS = 0, sumCycles = 0;

    public void reloadInstructions(ProgramService ps, int degree, List<InstructionDTO> instructionsDTO) {
        ps.loadExpasionByDegree(degree);
        var prog = ps.getProgram();
        //var dtos = (prog != null) ? prog.getInstructionDTOs() : List.<InstructionDTO>of();
        var dtos = instructionsDTO;
        countB = 0; countS = 0; sumCycles = 0;
        for (var dto : dtos) {
            try { sumCycles += Integer.parseInt(dto.getCycles()); } catch (NumberFormatException ignore) {}
            if ("B".equalsIgnoreCase(dto.getType())) countB++; else countS++;
        }

        root = new TreeItem<>(null);
        for (var dto : dtos) root.getChildren().add(new TreeItem<>(dto));

        if (expandroot == null) expandroot = new TreeItem<>();
        expandroot.getChildren().clear();
        expandroot.setExpanded(true);
    }

    public void onExpand(List<InstructionDTO> dtos,InstructionDTO current) {
        if (expandroot == null) expandroot = new TreeItem<>();
        expandroot.getChildren().clear();
        if (dtos != null && !dtos.isEmpty()) {
            dtos.addFirst(current);
            for (InstructionDTO dto : dtos) {
                expandroot.getChildren().add(new TreeItem<>(dto));
            }
            expandroot.setExpanded(true);
        }
        else {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Expand");
            a.setHeaderText(null);
            a.setContentText("No Expand to show.");
            a.showAndWait();
        }
    }

    public TreeItem<InstructionDTO> getRoot() { return root; }
    public TreeItem<InstructionDTO> getExpandRoot() { return expandroot; }

    public String buildSummary(ProgramService ps) {
        int size = (root == null) ? 0 : root.getChildren().size();
        return "Program loaded " + ps.getProgramName()
                + " | Instructions: " + size
                + " | B/S: " + countB + "/" + countS;
    }
}
