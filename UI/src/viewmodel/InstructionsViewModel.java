package viewmodel;

import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import logic.dto.InstructionDTO;
import logic.instructions.InstructionData;
import services.ProgramService;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InstructionsViewModel {

    private TreeItem<InstructionDTO> root;
    private TreeItem<InstructionDTO> expandroot;

    private int sumCycles = 0;
    private int countI = 0, countII = 0, countIII = 0, countIV = 0;

    private final EnumMap<Architecture, Set<Integer>> unsupportedByArch =
            new EnumMap<>(Architecture.class);

    public InstructionsViewModel() {
        for (Architecture arch : Architecture.values()) {
            unsupportedByArch.put(arch, new HashSet<>());
        }
    }

    public void reloadInstructions(ProgramService ps, int degree, List<InstructionDTO> instructionsDTO) {
        ps.loadExpansionByDegree(degree);

        sumCycles = 0;
        countI = 0; countII = 0; countIII = 0; countIV = 0;

        // נקה סטים קודמים
        unsupportedByArch.values().forEach(Set::clear);

        // בנה רוט חדש לעץ
        root = new TreeItem<>(null);

        for (int index = 0; index < instructionsDTO.size(); index++) {
            InstructionDTO dto = instructionsDTO.get(index);

            try { sumCycles += Integer.parseInt(dto.getCycles()); } catch (NumberFormatException ignore) {}

            InstructionData data = null;
            try {
                data = InstructionData.valueOf(dto.getInstructionName());
            } catch (IllegalArgumentException ignored) {
                data = null;
            }

            // סימון לא-נתמכות ישירות
            for (Architecture arch : Architecture.values()) {
                boolean supported = (data != null) && arch.supports(data);
                if (!supported) {
                    unsupportedByArch.get(arch).add(index);
                }
            }

            // בניית ה-Tree
            root.getChildren().add(new TreeItem<>(dto));
        }

        int size = root.getChildren().size();
        countI   = size - unsupportedByArch.get(Architecture.I).size();
        countII  = size - unsupportedByArch.get(Architecture.II).size();
        countIII = size - unsupportedByArch.get(Architecture.III).size();
        countIV  = size - unsupportedByArch.get(Architecture.IV).size();

        if (expandroot == null) expandroot = new TreeItem<>();
        expandroot.getChildren().clear();
        expandroot.setExpanded(true);
    }

    public int getSumCycles() {
        return sumCycles;
    }

    public void onExpand(List<InstructionDTO> dtos, InstructionDTO current) {
        if (expandroot == null) expandroot = new TreeItem<>();
        expandroot.getChildren().clear();

        if (dtos != null && !dtos.isEmpty()) {
            List<InstructionDTO> toShow = new ArrayList<>(dtos.size() + 1);
            toShow.add(current);
            toShow.addAll(dtos);
            for (InstructionDTO dto : toShow) {
                expandroot.getChildren().add(new TreeItem<>(dto));
            }
            expandroot.setExpanded(true);
        } else {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Expand");
            a.setHeaderText(null);
            a.setContentText("No Expand to show.");
            a.showAndWait();
        }
    }

    public TreeItem<InstructionDTO> getRoot() {
        return root;
    }

    public TreeItem<InstructionDTO> getExpandRoot() {
        return expandroot;
    }

    /** תקציר */
    public String buildSummary(ProgramService ps) {
        int size = (root == null) ? 0 : root.getChildren().size();
        return "Program loaded: " + ps.getProgramName()
                + " | Instructions: " + size
                + " | I: " + countI + "/" + size
                + " | II: " + countII + "/" + size
                + " | III: " + countIII + "/" + size
                + " | IV: " + countIV + "/" + size;
    }

    // --- מונים ---
    public int getCountI()   { return countI; }
    public int getCountII()  { return countII; }
    public int getCountIII() { return countIII; }
    public int getCountIV()  { return countIV; }

    // --- סטים של אינדקסים לא נתמכים (בלתי ניתנים לשינוי כלפי חוץ) ---
    public Set<Integer> getArchitecturesI_Indexes()   { return Set.copyOf(unsupportedByArch.get(Architecture.I)); }
    public Set<Integer> getArchitecturesII_Indexes()  { return Set.copyOf(unsupportedByArch.get(Architecture.II)); }
    public Set<Integer> getArchitecturesIII_Indexes() { return Set.copyOf(unsupportedByArch.get(Architecture.III)); }
    public Set<Integer> getArchitecturesIV_Indexes()  { return Set.copyOf(unsupportedByArch.get(Architecture.IV)); }

    /** להמרה ל-List כשקומפוננטת ה-UI שלך דורשת List<Integer> */
    public List<Integer> getUnsupportedAsList(String architecture) {
        Architecture arch = parseArch(architecture);
        return new ArrayList<>(unsupportedByArch.get(arch));
    }

    /** מיפוי נוח לשמות מה-UI: "IV" → IX, אחרת valueOf רגיל (I/II/III) */
    private Architecture parseArch(String s) {
        if (s == null) throw new IllegalArgumentException("architecture is null");
        if ("IV".equalsIgnoreCase(s)) return Architecture.IV;
        return Architecture.valueOf(s);
    }

    /** עוזר ל-UI שלך שמסמן שורות: מקבל שם ארכיטקטורה כטקסט וממיר ל-List */
    public void setHighlitedRowIndexAccordingToArchitecture(String architecture) {
        if (architecture == null || architecture.isEmpty()) return;
        List<Integer> unsupported = getUnsupportedAsList(architecture);
        setHighLightedRowIndexes(unsupported);
    }

    /** מממש ע"י ה-View / Controller שלך */
    private void setHighLightedRowIndexes(List<Integer> indexes) {
        // TODO: לממש את לוגיקת הסימון בפועל ב-UI (TableView/ListView וכו')
        // השארתי כאן חתימה בלבד כדי שתוכל לחבר בקלות לקוד הקיים שלך.
    }
}