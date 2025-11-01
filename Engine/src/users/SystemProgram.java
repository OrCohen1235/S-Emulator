package users;

import engine.Engine;
import jaxbsprogram.ReadSemulatorXml;
import logic.dto.ProgramDTO;
import logic.function.Function;
import logic.instructions.Instruction;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.*;

public class SystemProgram {

    // מידע בסיסי
    private final String name;
    private final String uploaderUsername;
    List<Function> functions = new ArrayList<>();
    List<Instruction> instructions = new ArrayList<>();

    // ה-XML הגולמי - שמור בזיכרון!
    private final byte[] xmlContent;
    private Engine engine;

    // מטא-דאטה על התוכנית
    private final ProgramDTO programDTO;

    // סטטיסטיקות
    private int runCount = 0;
    private double totalCreditsCost = 0.0;

    public SystemProgram(String uploaderUsername, InputStream xmlStream)
            throws Exception {

        this.uploaderUsername = uploaderUsername;

        // 1. שמירת ה-XML בזיכרון
        this.xmlContent = xmlStream.readAllBytes();

        Engine tempEngine = new Engine(new ByteArrayInputStream(xmlContent));

        if (!tempEngine.getLoaded()) {
            throw new IllegalArgumentException("Failed to load program from XML");
        }
        this.engine = tempEngine;
        this.programDTO = tempEngine.getProgramDTO();
        this.functions.addAll(programDTO.getFunctions());
        this.name = programDTO.getProgramName();


        System.out.println("[SystemProgram] Created: " + name +
                " (uploader: " + uploaderUsername + ")");
    }

    public List<Function> getFunctions() {
        for (Function function : functions) {
            function.getMaxDegree();
        }
        return functions;
    }

    /**
     * יצירת Engine חדש לכל הרצה - זה הקסם!
     * כל הרצה מקבלת מנוע נקי ועצמאי
     */
    public Engine createFreshEngine() {
        Engine newEngine = new Engine(new ByteArrayInputStream(xmlContent));
        this.engine = newEngine;
        this.functions.addAll(programDTO.getFunctions());
        return newEngine;
    }

    public Engine createFreshEngine(List<ReadSemulatorXml> allReadSem) {
        Engine newEngine = new Engine(new ByteArrayInputStream(xmlContent),allReadSem);
        this.engine = newEngine;
        this.functions.addAll(programDTO.getFunctions());
        return newEngine;
    }

    public List<Instruction> getInstructions1() {
        return programDTO.getProgram().getActiveInstructions();
    }

    // ========== Getters - מידע בסיסי ==========

    public String getName() {
        return name;
    }

    public String getUploaderUsername() {
        return uploaderUsername;
    }

    public ProgramDTO getProgramDTO() {
        return programDTO;
    }

    public byte[] getXmlContent() {
        return xmlContent.clone(); // מחזיר עותק לבטיחות
    }

    public void setFunctionsToEngine(Set<Function> allFunctions) {
        List<Function> functionList = new ArrayList<>(allFunctions);
        this.engine.getProgramDTO().setFunctions(functionList);
    }

    public Engine getEngine() {
        return engine;
    }

    // ========== Getters - מידע מחושב ==========

    public int getInstructionCount() {
        return programDTO.getInstructionDTOs().size();
    }

    public int getMaxDegree() {
        return programDTO.getMaxDegree();
    }

    // ========== Getters - סטטיסטיקות ==========

    public int getRunCount() {
        return runCount;
    }

    public double getTotalCreditsCost() {
        return totalCreditsCost;
    }

    public double getAverageCost() {
        return runCount > 0 ? totalCreditsCost / runCount : 0.0;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    // ========== Actions ==========

    /**
     * רישום הרצה חדשה
     * @param creditsCost כמה קרדיטים עלתה ההרצה
     */
    public synchronized void addRun(double creditsCost) {
        runCount++;
        totalCreditsCost += creditsCost;
    }

    @Override
    public String toString() {
        return String.format(
                "SystemProgram{name='%s', uploader='%s', instructions=%d, maxDegree=%d, runs=%d, avgCost=%.2f}",
                name, uploaderUsername, getInstructionCount(), getMaxDegree(), runCount, getAverageCost()
        );
    }
}