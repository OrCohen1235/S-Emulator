package menu;

import Logic.DTO.ProgramDTO;
import engine.Engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {
    private final Scanner in = new Scanner(System.in);
    private Engine engine;
    private ProgramDTO programDTO;
    private int runDegreeATM=0;
    private List<History> history=new ArrayList<>();
    private int historySize=0;

    public Menu() {}

    private void printMainMenu(boolean hasProgram) {
        System.out.println("=====================================");
        System.out.println("Menu:");
        System.out.println("1) Load XML");
        System.out.println("2) Show program" + (hasProgram ? "" : "  (disabled: no program loaded)"));
        System.out.println("3) Expand" + (hasProgram ? "" : "            (disabled: no program loaded)"));
        System.out.println("4) Run program" + (hasProgram ? "" : "      (disabled: no program loaded)"));
        System.out.println("5) Show history" + (hasProgram ? "" : "     (disabled: no program loaded)"));
        System.out.println("6) Exit");
    }

    public void run() {
        boolean hasProgram = false;
        do {
            printMainMenu(hasProgram);
            int choice = askIntInRange("Choose an option (1-6): \n",1,6);

            switch (choice) {
                case 1 -> cmdLoadXml();
                case 2 -> { if (ensureProgramLoaded()) cmdShowProgram(); }
                case 3 -> { if (ensureProgramLoaded()) cmdExpand(); }
                case 4 -> { if (ensureProgramLoaded()) cmdRun(); }
                case 5 -> { if (ensureProgramLoaded()) cmdHistory(); }
                case 6 -> { System.out.println("Bye!"); return; }
                default -> System.out.println("Invalid option. Please try again.\n");
            }
            System.out.println();
        } while (true);
    }

    // inside Menu (or any UI class)
    private void cmdHistory() {
        if (history == null) {
            System.out.println("No history to show.");
            return;
        }
        System.out.println("===== Run History Entry =====");
        for (History h : history) {
            System.out.println("Run #:     " + h.getNumberofPrograms());
            System.out.println("Degree:    " + h.getDegree());
            System.out.println("Inputs:    " + formatInputs(h.getxValues()));
            System.out.println("y (result): " + (h.getFinalResult() == null ? "null" : h.getFinalResult()));
            System.out.println("Cycles:    " + h.getFinalCycles());
            System.out.println("=============================");
        }
    }

    // helper to format the inputs list as CSV
    private static String formatInputs(java.util.List<Long> xs) {
        if (xs == null || xs.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < xs.size(); i++) {
            if (i > 0) sb.append(',');
            Long v = xs.get(i);
            sb.append(v == null ? "null" : v.toString());
        }
        return sb.toString();
    }


    private void cmdRun() {
        engine.loadExpansion();
        System.out.println("Max Degree is: "+ engine.getMaxDegree());
        runDegreeATM=askIntInRange("Choose degree: ",0,engine.getMaxDegree());
        History newHistory = new History();

        if (runDegreeATM!=0){engine.loadExpansionByDegree(runDegreeATM);}
        programDTO.getVariables().forEach(variable -> {
            System.out.println("Variable: " + variable);
        });

        List<Long> values = readCsvLongsFromUser();


        engine.loadInputVars(values);

        Long finalResult=engine.runProgramExecutor(runDegreeATM);

        programDTO.getVarsValues().forEach((name, val) ->
                System.out.println("Variable: " + name + " = " + val)
        );
        System.out.println("\nTotal Cycles: " + engine.getSumOfCycles());


        newHistory.setxValues(values);
        newHistory.setDegree(runDegreeATM);
        newHistory.setFinalResult(finalResult);

        newHistory.setFinalCycles(engine.getSumOfCycles());

        newHistory.setNumberofPrograms(historySize+1);
        history.add(newHistory);
        engine.setSumOfCycles();
        historySize++;
    }

    private void cmdExpand() {
        engine.loadExpansion();
        int maxDegree = engine.getMaxDegree();
        System.out.println("Max Degree is: "+maxDegree);
        runDegreeATM = askIntInRange("Choose a degree between 0 and:" +maxDegree,0, maxDegree);

       List<String> resultExpandCommands = engine.getListOfExpandCommands(runDegreeATM);
       printResultExpandsCommands(resultExpandCommands);

    }

    private void printResultExpandsCommands(List<String> lstExpandCommands) {
        System.out.println("Expand commands:");
        for (String expandCommand : lstExpandCommands) {
            System.out.println(expandCommand);
        }

    }

    private void cmdShowProgram() {
        System.out.println("ProgramName: \n"+programDTO.getProgramName());
        List<String> outVars= programDTO.getVariables();
        System.out.println("\nVariables: ");
        for (String outVar : outVars) {
            System.out.println(outVar);
        }
        System.out.println("\nLabels: ");
        List<String> outLables=programDTO.getLabels();
        for (String outLable : outLables) {
            System.out.println(outLable);
        }
        List<String> resultExpandCommands = engine.getListOfExpandCommands(runDegreeATM);
        printResultExpandsCommands(resultExpandCommands);

        //System.out.println("\nSumOfCycles: "+history.getLast().getFinalCycles());
    }

    private void cmdLoadXml() {
        while (true) {
            System.out.print("Enter full path to XML file (leave empty to cancel): ");
            String raw = in.nextLine();
            if (raw == null) raw = "";
            String path = raw.trim();

            if (path.isEmpty()) {
                System.out.println("Load cancelled.");
                return;
            }

            if ((path.startsWith("\"") && path.endsWith("\"")) ||
                    (path.startsWith("'")  && path.endsWith("'"))) {
                path = path.substring(1, path.length() - 1).trim();
            }

            if (!path.toLowerCase(Locale.ROOT).endsWith(".xml")) {
                System.out.println("Error: file must end with .xml (case-insensitive).");
                continue;
            }

            Pattern englishPath = Pattern.compile("^[A-Za-z0-9_ .:/\\\\-]+$");
            if (!englishPath.matcher(path).matches()) {
                System.out.println("Error: path must contain only English letters/digits (spaces allowed).");
                continue;
            }

            File f = new File(path);
            if (!f.exists()) {
                System.out.println("Error: file does not exist.");
                continue;
            }
            if (!f.isFile()) {
                System.out.println("Error: path is not a file.");
                continue;
            }
            if (!f.canRead()) {
                System.out.println("Error: file is not readable (permissions).");
                continue;
            }
            if (f.length() == 0L) {
                System.out.println("Error: file is empty.");
                continue;
            }
            try {
                Engine temp = new Engine(f);

                if (temp.getLoaded()) {
                    engine=new Engine(f);
                    programDTO=engine.getProgramDTO();
                    System.out.println("✓ Program loaded successfully: " + programDTO.getProgramName());
                    return; // הצלחה
                } else {
                    System.out.println("✗ Invalid XML (application-wise). The previous valid program remains active.");
                    continue;
                }

            } catch (Exception e) {
                System.out.println("✗ Failed to load XML: " + e.getClass().getSimpleName() +
                        (e.getMessage() != null ? " - " + e.getMessage() : ""));
                System.out.println("The previous valid program (if existed) remains active.");
                continue;
            }
        }
    }


    private boolean ensureProgramLoaded() {
        if (engine == null || !engine.getLoaded() ) {
            System.out.println("No valid program is currently loaded.");
            return false;
        }
        return true;
    }

    private int askIntInRange(String prompt,int min, int max) {
        while (true) {
            System.out.print(prompt);
            String s = in.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= min && v <= max) return v;
            } catch (NumberFormatException ignored) {}
            System.out.println("Invalid number. Please try again.");
        }
    }

    private static final Pattern CSV_INTS_LINE =
            Pattern.compile("^\\s*\\d+(\\s*,\\s*\\d+)*\\s*,?\\s*$");

    private List<Long> readCsvLongsFromUser() {
        while (true) {
            System.out.print("Enter comma-separated integers (e.g., 1,2,3,): ");
            String raw = in.nextLine();
            if (raw == null) raw = "";
            raw = raw.trim();

            // בדיקת פורמט כללי (רק ספרות ופסיקים; רווחים מותר)
            if (!CSV_INTS_LINE.matcher(raw).matches()) {
                System.out.println("Invalid input. Use only integers separated by commas, e.g. 1,2,3,");
                continue;
            }

            // בשלב הזה הפורמט תקין; מפצל לפסיקים (רווחים ייחתכו)
            String[] parts = raw.split("\\s*,\\s*"); // לא שומר אלמנט ריק מסיום בפסיק
            List<Long> out = new ArrayList<>(parts.length);

            boolean ok = true;
            for (String p : parts) {
                try {
                    // כאן בטוח ספרות בלבד; עדיין עלול לזרוק אם חורג מטווח long
                    out.add(Long.parseLong(p));
                } catch (NumberFormatException ex) {
                    ok = false;
                    break;
                }
            }

            if (ok) return out; // הצלחה
            System.out.println("Invalid number detected. Please enter only 64-bit integers like: 1,2,3,");
        }
    }




}
