package jaxbsprogram;

import generated.*;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ReadSemulatorXml {
    private static final Pattern ENGLISH_PATH = Pattern.compile("[A-Za-z0-9\\\\/:._\\-() ]+");

    private SProgram simulator;
    private InputStream fileContext;// Holds the unmarshalled program data
     // XML file reference

    /** New: constructor that accepts a raw path string and validates it */
    public ReadSemulatorXml(InputStream fileContext) {
        this.fileContext = fileContext;
        loadFiles();
    }

    /** Original constructor kept (still validates existence/isFile implicitly by JAXB load) */


    private void loadFiles() {
        try {
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            this.simulator = (SProgram) unmarshaller.unmarshal(fileContext); // Convert XML to SProgram object
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to unmarshal XML from: " + "\n", e);
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
                .filter(this::checkIfLabel)
                .filter(lbl -> !"EXIT".equals(lbl))
                .filter(lbl -> list.stream().noneMatch(i -> Objects.equals(i.getSLabel(), lbl)))
                .reduce((first, second) -> second)
                .orElse("");
    }

    public String checkFunctionValidity() {
        List<SFunction> functions = getSFunctionList();
        List<SInstruction> mainInstructions = getSInstructionList();

        Set<String> definedFunctions = new HashSet<>();
        for (SFunction func : functions) {
            definedFunctions.add(func.getName());
        }

        String error = checkInstructionsForUndefinedFunctions(mainInstructions, definedFunctions, "main program");
        if (!error.isEmpty()) {
            return error;
        }

        for (SFunction function : functions) {
            List<SInstruction> functionInstructions = function.getSInstructions().getSInstruction();
            error = checkInstructionsForUndefinedFunctions(
                    functionInstructions,
                    definedFunctions,
                    "function '" + function.getName() + "'"
            );
            if (!error.isEmpty()) {
                return error;
            }
        }

        return "";
    }

    private String checkInstructionsForUndefinedFunctions(
            List<SInstruction> instructions,
            Set<String> definedFunctions,
            String context) {

        for (SInstruction inst : instructions) {
            if (inst.getSInstructionArguments() == null) {
                continue;
            }

            List<SInstructionArgument> args = inst.getSInstructionArguments().getSInstructionArgument();
            String instructionName = inst.getName();

            if ("QUOTE".equals(instructionName) || "JUMP_EQUAL_FUNCTION".equals(instructionName)) {
                if (!args.isEmpty()) {
                    String functionName = args.get(0).getValue();

                    if (checkIfLabel(functionName)) {
                        continue; // זו תווית, לא פונקציה - הכל בסדר
                    }

                    if (!definedFunctions.contains(functionName)) {
                        return "Error in " + context + ": Function '" + functionName + "' is not defined";
                    }
                }
            }
        }

        return "";
    }

    private boolean checkIfLabel(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        if ("EXIT".equals(str)) {
            return true;
        }

        if (str.charAt(0) == 'L') {
            return true;
        }

        List<SInstruction> instructions = getSInstructionList();
        for (SInstruction inst : instructions) {
            if (str.equals(inst.getSLabel())) {
                return true;
            }
        }

        List<SFunction> functions = getSFunctionList();
        for (SFunction func : functions) {
            List<SInstruction> funcInstructions = func.getSInstructions().getSInstruction();
            for (SInstruction inst : funcInstructions) {
                if (str.equals(inst.getSLabel())) {
                    return true;
                }
            }
        }

        return false;
    }
//    private boolean checkIfLabel(String str) {
//        return str != null && !str.isEmpty() && (str.charAt(0) == 'L' || str.equals("EXIT"));
//    }

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
