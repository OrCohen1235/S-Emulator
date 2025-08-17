package menu;

import Logic.DTO.ProgramDTO;
import engine.Engine;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Menu {
    private final Scanner in = new Scanner(System.in);
    private Engine engine;
    private ProgramDTO programDTO;

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
        System.out.print("Choose an option: ");
    }

    public void run() {
        boolean hasProgram = false;
        do {
            printMainMenu(hasProgram); // אפשר לשקול להעביר כאן engine.getLoaded()
            String choice = in.nextLine().trim();

            switch (choice) {
                case "1" -> cmdLoadXml();
                case "2" -> { if (ensureProgramLoaded()) cmdShowProgram(); }
                case "3" -> { if (ensureProgramLoaded()) cmdExpand(); }
                case "4" -> { if (ensureProgramLoaded()) cmdRun(); }
                case "5" -> { if (ensureProgramLoaded()) cmdHistory(); }
                case "6" -> { System.out.println("Bye!"); return; }
                default -> System.out.println("Invalid option. Please try again.");
            }
            System.out.println();
            hasProgram = engine.getLoaded();
        } while (true);
    }

    private void cmdHistory() {
    }

    private void cmdRun() {
        System.out.println("Max Degree is: "+ programDTO.getMaxDegree());
        programDTO.getVariables().forEach(variable -> {
            System.out.println("Variable: " + variable);
        });
        System.out.print("Enter comma-separated numbers: ");
        String line = in.nextLine();
        List<Long> values = parseCsvLongs(line);
        engine.loadInputVars((ArrayList<Long>) values);

        System.out.println(engine.runProgramExecutor());
    }

    private void cmdExpand() {
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
        List<String> outCommands=programDTO.getCommands();
        System.out.println("\nCommands: ");
        for (String outCommand : outCommands) {
            System.out.println(outCommand);
        }
    }

    private void cmdLoadXml() {
        System.out.print("Enter full path to XML file: ");
        String path = in.nextLine().trim();

        if (!path.toLowerCase(Locale.ROOT).endsWith(".xml")) {
            System.out.println("Error: file must end with .xml");
            return;
        }
        File f = new File(path);
        if (!f.exists() || !f.isFile()) {
            System.out.println("Error: file does not exist or path is invalid.");
            return;
        }
        Pattern englishPath = Pattern.compile("^[A-Za-z0-9_ .:/\\\\-]+$");
        if (!englishPath.matcher(path).matches()) {
            System.out.println("Error: path must contain English letters/digits (spaces allowed).");
            return;
        }

        this.engine = new Engine(f);
        engine.setLoaded(true);
        if (engine.getLoaded()) {
            this.programDTO = engine.getProgramDTO();
            System.out.println("✓ Program loaded successfully: " + programDTO.getProgramName());
        } else {
            System.out.println(f.toString()+"Invalid XML (application-wise).");
            System.out.println("The previous valid program (if existed) remains active.");
        }
    }

    private boolean ensureProgramLoaded() {
        if (!engine.getLoaded()) {
            System.out.println("No valid program is currently loaded.");
            return false;
        }
        return true;
    }

    private int askIntInRange(String prompt, int min, int max) {
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

    public ArrayList<Long> parseCsvLongs(String raw) {
        if (raw == null || raw.isBlank()) return null;
        ArrayList<Long> out = new ArrayList<>();
        for (String part : raw.split(",")) {
            String t = part.trim();
            if (t.isEmpty()) continue;
            try {
                out.add(Long.parseLong(t));   // אם אתה רוצה int: Integer.parseInt
            } catch (NumberFormatException ignored) {
                // אפשר גם להזהיר כאן: System.out.println("Warning: '" + t + "' is not a number");
            }
        }
        return out;
    }


}
