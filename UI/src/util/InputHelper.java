package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

public class InputHelper {
    private static final Pattern ENGLISH_PATH = Pattern.compile("^[A-Za-z0-9_ .:/\\\\-]+$");
    private static final Pattern CSV_INTS_LINE = Pattern.compile("^\\s*\\d+(\\s*,\\s*\\d+)*\\s*,?\\s*$");

    public int askIntInRange(Scanner in, String prompt, int min, int max) {
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

    public List<Long> readCsvLongsFromUser(Scanner in) {
        while (true) {
            System.out.print("Enter comma-separated integers (e.g., 1,2,3,): ");
            String raw = in.nextLine();
            if (raw == null) raw = "";
            raw = raw.trim();

            if (!CSV_INTS_LINE.matcher(raw).matches()) {
                System.out.println("Invalid input. Use only integers separated by commas, e.g. 1,2,3,");
                continue;
            }
            String[] parts = raw.split("\\s*,\\s*");
            List<Long> out = new ArrayList<>(parts.length);
            try {
                for (String p : parts) out.add(Long.parseLong(p));
                return out;
            } catch (NumberFormatException ex) {
                System.out.println("Invalid number detected. Please enter only 64-bit integers like: 1,2,3,");
            }
        }
    }

    /** returns validated path, or null if cancelled */
    public String readValidXmlPathOrCancel(Scanner in) {
        while (true) {
            System.out.print("Enter full path to XML file (leave empty to cancel): ");
            String raw = in.nextLine();
            if (raw == null) raw = "";
            String path = raw.trim();

            if (path.isEmpty()) { System.out.println("Load cancelled."); return null; }
            if ((path.startsWith("\"") && path.endsWith("\"")) ||
                    (path.startsWith("'")  && path.endsWith("'"))) {
                path = path.substring(1, path.length() - 1).trim();
            }
            if (!path.toLowerCase(Locale.ROOT).endsWith(".xml")) {
                System.out.println("Error: file must end with .xml (case-insensitive)."); continue;
            }
            if (!ENGLISH_PATH.matcher(path).matches()) {
                System.out.println("Error: path must contain only English letters/digits (spaces allowed)."); continue;
            }

            File f = new File(path);
            if (!f.exists()) { System.out.println("Error: file does not exist."); continue; }
            if (!f.isFile())  { System.out.println("Error: path is not a file."); continue; }
            if (!f.canRead()) { System.out.println("Error: file is not readable (permissions)."); continue; }
            if (f.length() == 0L) { System.out.println("Error: file is empty."); continue; }

            return path;
        }
    }
}
