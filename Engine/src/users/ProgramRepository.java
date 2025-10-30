package users;

import logic.function.Function;
import logic.instructions.Instruction;
import logic.instructions.sinstruction.JumpEqualFunction;
import logic.instructions.sinstruction.Quote;
import program.Program;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProgramRepository {

    // מפה: שם_תוכנית → SystemProgram
    private final Map<String, SystemProgram> programs = new ConcurrentHashMap<>();
    private Set<SystemFunction> systemFunctions = new HashSet<>();
    private Set<Function> allFunctions = new HashSet<>();

    public synchronized SystemProgram uploadProgram(String uploaderUsername, InputStream xmlStream) throws Exception {

        // 1. יצירת SystemProgram חדש
        SystemProgram program = new SystemProgram(uploaderUsername, xmlStream);

        String programName = program.getName();

        // 2. בדיקה אם התוכנית כבר קיימת
        if (programs.containsKey(programName)) {
            throw new IllegalArgumentException(
                    "Program '" + programName + "' already exists in the system"
            );
        }

        List<Function> functions = program.getFunctions();

        for (Function function : functions) {
            for (Function function2 : allFunctions) {
                if (function.getName().equals(function2.getName())) {
                    throw new IllegalArgumentException("Program '" + programName + "' Contains function: " + function.getName() +  " that already exists in the system");
                }
            }
        }

        List<Instruction> instructions = program.getInstructions();
        Set<Function> tempFunctionSet = new HashSet<>();
        tempFunctionSet.addAll(allFunctions);
        tempFunctionSet.addAll(functions);

        for (Instruction instruction : instructions) {
            if (instruction instanceof Quote) {
                String functionName = ((Quote) instruction).getFunctionName();
                boolean functionExists = false;

                for (Function function : tempFunctionSet) {
                    if (function.getName().equals(functionName)) {
                        functionExists = true;
                        break;
                    }
                }

                if (!functionExists) {
                    throw new IllegalArgumentException("Program '" + programName +
                            "' use function: " + functionName + " that does not exist in the system");
                }
            } else if (instruction instanceof JumpEqualFunction) {
                String funcName = ((JumpEqualFunction) instruction).getFuncName();
                boolean functionExists = false;

                for (Function function : tempFunctionSet) {
                    if (function.getName().equals(funcName)) {
                        functionExists = true;
                        break;
                    }
                }

                if (!functionExists) {
                    throw new IllegalArgumentException("Program '" + programName +
                            "' uses a function: " + funcName + " that does not exist in the system");
                }
            }
        }

        // 3. שמירה במאגר
        programs.put(programName, program);
        createsFunctions();
        System.out.println("[ProgramRepository] Uploaded: " + programName +
                " by " + uploaderUsername);

        return program;
    }

    public Map<String, SystemProgram> getPrograms() {
        return programs;
    }

    public Set<SystemFunction> getSystemFunctions() {
        return systemFunctions;
    }

    public Set<Function> getAllFunctions() {
        return allFunctions;
    }

    private void setAllFunctionsToPrograms(){
        for (SystemProgram program : programs.values()) {
            program.setFunctionsToEngine(allFunctions);
        }
    }

    public SystemProgram getProgram(String programName) {
        SystemProgram program = programs.get(programName);

        if (program == null) {
            throw new NoSuchElementException(
                    "Program '" + programName + "' not found in repository"
            );
        }

        return program;
    }

    public SystemFunction getFunction(String programName) {
        SystemFunction program = systemFunctions.stream()
                .filter(f -> f.getFunctionName().equals(programName)) // או איך שהשם נשמר
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException(
                        "Program '" + programName + "' not found in repository"
                ));

        return program;
    }

    public void createsFunctions(){
        for (SystemProgram program : programs.values()) {
            allFunctions.addAll(program.getFunctions());
            System.out.println("[ProgramRepository] Created Functions: " + allFunctions.size());
            for (Function function : program.getFunctions()) {
                SystemFunction systemFunction = new SystemFunction(function.getName(),
                        program.getName(),
                        program.getUploaderUsername(),
                        function.getSizeOfInstructions(),
                        function);
                systemFunctions.add(systemFunction);
            }
        }

        setAllFunctionsToPrograms();

    }

    /**
     * בדיקה אם תוכנית קיימת במאגר
     */
    public boolean programExists(String programName) {
        return programs.containsKey(programName);
    }

    /**
     * קבלת כל התוכניות במאגר
     * @return Collection לא ניתנת לשינוי
     */
    public Collection<SystemProgram> getAllPrograms() {
        return Collections.unmodifiableCollection(programs.values());
    }

    /**
     * מחיקת תוכנית מהמאגר
     *
     * @param programName שם התוכנית למחיקה
     * @return SystemProgram שנמחק (או null אם לא היה)
     */
    public synchronized SystemProgram removeProgram(String programName) {
        SystemProgram removed = programs.remove(programName);

        if (removed != null) {
            System.out.println("[ProgramRepository] Removed: " + programName);
        }

        return removed;
    }

    /**
     * מספר התוכניות במאגר
     */
    public int getProgramCount() {
        return programs.size();
    }

    /**
     * הדפסת כל התוכניות (debug)
     */
    public void printAllPrograms() {
        System.out.println("=== Programs in Repository ===");
        for (SystemProgram program : programs.values()) {
            System.out.println("  " + program);
        }
        System.out.println("Total: " + programs.size());
    }

    /**
     * ניקוי כל התוכניות (shutdown)
     */
    public synchronized void clearAllPrograms() {
        programs.clear();
        System.out.println("[ProgramRepository] Cleared all programs");
    }
}