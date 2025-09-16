package jaxbsprogram;

import generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ReadSemulatorXml {
    private static final Pattern ENGLISH_PATH = Pattern.compile("[A-Za-z0-9\\\\/:._\\-() ]+");

    private SProgram simulator; // Holds the unmarshalled program data
    private final File xmlFile; // XML file reference

    /** New: constructor that accepts a raw path string and validates it */
    public ReadSemulatorXml(String rawPath) {
        String cleaned = validateXmlPath(rawPath);   // throws on any problem
        this.xmlFile = new File(cleaned);
        loadFiles();
    }

    /** Original constructor kept (still validates existence/isFile implicitly by JAXB load) */
    public ReadSemulatorXml(File file) {
        this.xmlFile = Optional.ofNullable(file)
                .orElseThrow(() -> new IllegalArgumentException("file is null"));

        Optional.of(xmlFile)
                .filter(f -> f.exists() && f.isFile())
                .orElseThrow(() -> new IllegalArgumentException(
                        "XML file not found: " + xmlFile.getAbsolutePath() + "\n"));

        loadFiles(); // Load and unmarshal the XML into simulator
    }

    private void loadFiles() {
        try {
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            this.simulator = (SProgram) unmarshaller.unmarshal(xmlFile); // Convert XML to SProgram object
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to unmarshal XML from: " + xmlFile.getAbsolutePath() + "\n", e);
        }
    }

    public String getProgramName() {
        return simulator.getName(); // Return program name from XML
    }

    public List<SInstruction> getSInstructionList() {
        return simulator.getSInstructions().getSInstruction(); // Return list of instructions
    }

    public List<SFunction> getSFunctionList() {
        if (simulator.getSFunctions()!=null) {
            return simulator.getSFunctions().getSFunction();
        }
        List<SFunction> functionList = new ArrayList<>();
        return functionList;

    }

    public String checkLabelValidity() {
        List<SInstruction> list = getSInstructionList();

        return list.stream()
                .flatMap(inst -> Stream.ofNullable(inst.getSInstructionArguments())
                        .flatMap(args -> args.getSInstructionArgument().stream()))
                .map(SInstructionArgument::getValue)
                .filter(this::checkIfLabel) // Only values that represent labels
                .filter(lbl -> list.stream().noneMatch(i -> Objects.equals(i.getSLabel(), lbl))) // Keep labels not defined
                .reduce((first, second) -> second) // Return last invalid label
                .orElse(""); // Empty string if all labels are valid
    }

    private boolean checkIfLabel(String str) {
        return str != null && !str.isEmpty() && str.charAt(0) == 'L'; // Label must start with 'L'
    }

    /** -------- New: validation logic moved into the engine (throws exceptions instead of printing) -------- */
    public static String validateXmlPath(String rawPath) {
        if (rawPath == null) throw new IllegalArgumentException("Path cannot be null.");

        String path = rawPath.trim();
        if (path.isEmpty()) throw new IllegalArgumentException("Path cannot be empty.");

        // Strip surrounding quotes if present
        if ((path.startsWith("\"") && path.endsWith("\"")) ||
                (path.startsWith("'")  && path.endsWith("'"))) {
            path = path.substring(1, path.length() - 1).trim();
        }

        // Must end with .xml (case-insensitive)
        if (!path.toLowerCase(Locale.ROOT).endsWith(".xml")) {
            throw new IllegalArgumentException("File must end with .xml (case-insensitive).");
        }

        // Only English letters/digits and common path chars (spaces allowed)
        if (!ENGLISH_PATH.matcher(path).matches()) {
            throw new IllegalArgumentException(
                    "Path may contain only English letters, digits, spaces, and \\\\/ : . _ - characters.");
        }

        File f = new File(path);
        if (!f.exists())   throw new IllegalArgumentException("File does not exist.");
        if (!f.isFile())   throw new IllegalArgumentException("Path is not a file.");
        if (!f.canRead())  throw new IllegalArgumentException("File is not readable (permissions).");
        if (f.length() == 0L) throw new IllegalArgumentException("File is empty.");

        return path;
    }
}
