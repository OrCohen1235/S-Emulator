package logic.instructions.sinstruction;

import logic.function.Function;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import logic.variable.VariableImpl;
import logic.variable.VariableType;
import program.Program;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Quote extends Instruction implements SyntheticInstruction {
    private String functionName;
    private String functionArguments;
    private final String originalArguments;
    private final Function function;
    private static final Pattern X_VAR = Pattern.compile("\\b([xyzXYZ]\\w*)\\b");

    public Quote(Program program, Variable var, Label label, String quoteName, String arguments) {
        super(program, InstructionData.QUOTE, var, label);
        this.functionName = quoteName;
        this.functionArguments = arguments;
        this.originalArguments = arguments;
        function = program.getFunctionByName(functionName);
        function.resetMapVariables();
        function.setMainProgram(getProgram());

    }

    private Quote(Program program, Variable var, Label label, String quoteName, String arguments, Program mainProgram) {
        super(program, InstructionData.QUOTE, var, label);
        this.functionName = quoteName;
        this.functionArguments = arguments;
        this.originalArguments = arguments;
        function = program.getFunctionByName(functionName);
        function.resetMapVariables();
        function.setMainProgram(mainProgram);
    }

    public long calcQuotationValue() {
        long y = function.getProgramExecutor().run();
        return y;
    }

    @Override
    public int calcCycles() {
        return 5 + function.getProgramExecutor().getSumOfCycles();
    }

    private void putVarsInMapsFromFather(Boolean check) {
        ArrayList<Long> vars = new ArrayList<>();
        if (functionArguments == null || functionArguments.isBlank()) return;

        // למניעת כפילויות בהכנסה ל-vars (לשמות חוקיים)
        Set<String> seenXYZ = new HashSet<>();
        // למניעת קריאה כפולה על אותו מזהה לא-חוקי
        Set<String> reported = new LinkedHashSet<>();

        // מזהה "מילים" כלליות בקלט
        final java.util.regex.Pattern IDENT   = java.util.regex.Pattern.compile("\\b([A-Za-z_]\\w*)\\b");
        // שמות מותרים: x<digits> | z<digits> | y (לא רגיש לאותיות)
        final java.util.regex.Pattern ALLOWED = java.util.regex.Pattern.compile("(?i)^(?:x\\d+|z\\d+|y)$");

        for (String part : functionArguments.split(",")) {
            if (part == null) continue;
            String s = part.trim();
            if (s.isEmpty()) continue;

            java.util.regex.Matcher m = IDENT.matcher(s);
            while (m.find()) {
                String name = m.group(1);
                if (name.isEmpty()) continue;

                if (ALLOWED.matcher(name).matches()) {
                    // חוקי: נטפל לפי X/Z/Y
                    char prefix = Character.toLowerCase(name.charAt(0));
                    boolean firstTime = seenXYZ.add(name.toLowerCase(java.util.Locale.ROOT));

                    switch (prefix) {
                        case 'x': {
                            Variable key = getVarFromMapByString(name);
                            Long value  = getValueFromMapByString(name);
                            if (key != null && value != null) {
                                function.setXVariablesToMap(key, value);
                            }
                            if (firstTime) vars.add(value);
                            break;
                        }
                        case 'z': {
                            Variable key = getVarFromMapByString(name);
                            Long value  = getValueFromMapByString(name);
                            if (key != null && value != null) {
                                function.setZVariablesToMap(key, value);
                            }
                            if (firstTime) vars.add(value);
                            break;
                        }
                        case 'y': {
                            Long value = getValueFromMapByString(name); // כאן name הוא בדיוק "y"
                            if (value != null) function.setY(value);
                            if (firstTime) vars.add(value);
                            break;
                        }
                    }
                } else {


                }
            }
        }

        if (!check) {
            function.setValuesToXMap(vars);
        }
    }

    /** קריאה כשנמצא מזהה שאינו x<i>/z<i>/y. אפשר להחליף ל-throw, log וכו'. */
    private void onNonXYZIdentifier(String name) {
        // דוגמה: לוג לאזהרה
        System.err.println("Non-XYZ identifier detected: " + name);
        // או:
        // throw new IllegalArgumentException("Unsupported identifier: " + name);
    }




    @Override
    public Label calculateInstruction() {
        // שלב 1: טעינת ערכי קלט מהאב
        putVarsInMapsFromFather(false);

        // שלב 2: עיבוד ארגומנטים עם קריאות מקוננות
        String processedArgs = processNestedCalls(functionArguments != null ? functionArguments : "");

        // שלב 3: טעינת הארגומנטים המעובדים לפונקציה
        functionArguments = processedArgs;
        loadInputsToFunction();

        // שלב 4: חישוב התוצאה
        long y = calcQuotationValue();
        setVarValueInMap(y);

        // שלב 5: החזרת הארגומנטים המקוריים
        setFunctionArgumentsToOriginal();

        return FixedLabel.EMPTY;
    }

    /**
     * מעבד קריאות מקוננות ומחזיר מחרוזת ארגומנטים מעובדת
     */
    private String processNestedCalls(String arguments) {
        if (arguments == null || arguments.trim().isEmpty()) {
            return arguments;
        }

        List<String> processedArgs = new ArrayList<>();
        List<String> topLevelArgs = splitTopLevelArguments(arguments);

        for (String arg : topLevelArgs) {
            String trimmedArg = arg.trim();
            if (trimmedArg.startsWith("(") && trimmedArg.endsWith(")")) {
                // זו קריאה מקוננת - נעבד אותה
                String processedCall = processNestedCall(trimmedArg);
                processedArgs.add(processedCall);
            } else {
                // זה ארגומנט רגיל
                processedArgs.add(trimmedArg);
            }
        }

        return String.join(",", processedArgs);
    }

    /**
     * מפצל ארגומנטים ברמה העליונה (לא בתוך סוגריים)
     */
    private List<String> splitTopLevelArguments(String arguments) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < arguments.length(); i++) {
            char ch = arguments.charAt(i);

            if (ch == '(') {
                depth++;
            } else if (ch == ')') {
                depth--;
            } else if (ch == ',' && depth == 0) {
                // פסיק ברמה העליונה - מסיימים ארגומנט נוכחי
                String arg = current.toString().trim();
                if (!arg.isEmpty()) {
                    result.add(arg);
                }
                current = new StringBuilder();
                continue;
            }

            current.append(ch);
        }

        // מוסיפים את הארגומנט האחרון
        String lastArg = current.toString().trim();
        if (!lastArg.isEmpty()) {
            result.add(lastArg);
        }

        return result;
    }

    /**
     * מעבד קריאה מקוננת יחידה
     */
    private String processNestedCall(String nestedCall) {
        // הסרת הסוגריים החיצוניים
        String inner = nestedCall.substring(1, nestedCall.length() - 1);

        // חילוץ שם הפונקציה והארגומנטים
        String functionName = extractFunctionNameFromCall(inner);
        String functionArgs = extractArgumentsFromCall(inner, functionName);

        // יצירת משתנה עבודה חדש
        //Variable workVar = getProgram().getExpanderExecute().getFreshWork();
        Variable workVar = new VariableImpl(VariableType.WORK, function.getMinFreeWorkIndex());
        // יצירת Quote חדש לקריאה המקוננת
        Quote childQuote = new Quote(function, workVar, FixedLabel.EMPTY, functionName, functionArgs, getProgram());

        // העברת כל המשתנים לילד
        transferAllVariablesToChild(childQuote);

        // הרצת הקריאה המקוננת
        childQuote.calculateInstruction();

        // החזרת שם המשתנה החדש
        return workVar.getRepresentation();
    }

    /**
     * מעביר את כל המשתנים הנוכחיים לקריאה הפנימית - גרסה מתוקנת
     */
    private void transferAllVariablesToChild(Quote child) {
        // העברת משתני X
        Map<Variable, Long> currentXVars = function.getxVariables();
        if (currentXVars != null) {
            for (Map.Entry<Variable, Long> entry : currentXVars.entrySet()) {
                child.function.setXVariablesToMap(entry.getKey(), entry.getValue());
            }
        }

        // העברת משתני Z
        Map<Variable, Long> currentZVars = function.getzVariables();
        if (currentZVars != null) {
            for (Map.Entry<Variable, Long> entry : currentZVars.entrySet()) {
                child.function.setZVariablesToMap(entry.getKey(), entry.getValue());
            }
        }

        // העברת Y
        Long currentY = function.getY();
        if (currentY != null) {
            child.function.setY(currentY);
        }

        // העברת המשתנים גם מהתוכנית הראשית אם יש
        Program mainProgram = function.getMainProgram();
        if (mainProgram != null && mainProgram != function) {
            Map<Variable, Long> mainXVars = mainProgram.getxVariables();
            if (mainXVars != null) {
                for (Map.Entry<Variable, Long> entry : mainXVars.entrySet()) {
                    child.function.setXVariablesToMap(entry.getKey(), entry.getValue());
                }
            }

            Map<Variable, Long> mainZVars = mainProgram.getzVariables();
            if (mainZVars != null) {
                for (Map.Entry<Variable, Long> entry : mainZVars.entrySet()) {
                    child.function.setZVariablesToMap(entry.getKey(), entry.getValue());
                }
            }
        }


//        List<Long> orderedValues = new ArrayList<>();
//        if (child.functionArguments != null && !child.functionArguments.isEmpty()) {
//            String[] args = child.functionArguments.split(",");
//            for (String arg : args) {
//                String trimmedArg = arg.trim();
//                if (!trimmedArg.isEmpty()) {
//                    Long val = child.function.getValueFromMapsByString(trimmedArg);
//                    if (val != null) {
//                        orderedValues.add(val);
//                    }
//                }
//            }
//        }
//        if (!orderedValues.isEmpty()) {
//            child.function.setValuesToXMap(orderedValues);
//        }
    }

    private String extractFunctionNameFromCall(String call) {
        if (call == null || call.trim().isEmpty()) return null;

        String trimmed = call.trim();
        int commaIndex = trimmed.indexOf(',');

        if (commaIndex == -1) {
            return trimmed;
        } else {
            return trimmed.substring(0, commaIndex).trim();
        }
    }

    /**
     * חולץ ארגומנטים מקריאה מקוננת
     */
    private String extractArgumentsFromCall(String call, String functionName) {
        if (call == null || functionName == null) return "";

        String trimmed = call.trim();
        int nameLength = functionName.length();

        if (nameLength >= trimmed.length()) return "";

        // מחפשים את הפסיק הראשון אחרי השם
        int commaIndex = trimmed.indexOf(',', nameLength);
        if (commaIndex == -1) return "";

        return trimmed.substring(commaIndex + 1).trim();
    }

    public Function getFunction() {
        return function;
    }

    private void loadInputsToFunction() {
        if (functionArguments == null || functionArguments.isBlank()) return;

        String[] parts = functionArguments.split(",");
        java.util.LinkedHashSet<String> orderedUnique = new java.util.LinkedHashSet<>();
        for (String p : parts) {
            String t = (p == null) ? "" : p.trim();
            if (!t.isEmpty()) orderedUnique.add(t);
        }

        List<Long> vars = new ArrayList<>(orderedUnique.size());
        for (String val : orderedUnique) {
            Long v = function.getValueFromMapsByString(val);
            vars.add(v);
        }
        function.setValuesToXMap(vars);
    }

    private static int firstOf(String s, int from, char... delims) {
        int best = -1;
        for (char d : delims) {
            int i = s.indexOf(d, from);
            if (i != -1 && (best == -1 || i < best)) best = i;
        }
        return best;
    }


    @Override
    public String getCommand() {
        String command = super.getVar().getRepresentation() + " <- " + "(" + function.getUserName();
        if (!functionArguments.isEmpty()){
            command += ", " + functionArguments.toUpperCase();
        }
        command += ")";
        return command;
    }

    public String getFunctionArguments() {
        return functionArguments;
    }

    public Variable getVarKeyFromMapByString(String val) {
        return function.getKeyFromMapsByString(val);
    }

    public List<Instruction> getInstructionsFromFunction() {
        return function.getInstructionList();
    }

    public void setFunctionArgumentsToOriginal() {
        this.functionArguments = originalArguments;
    }

    public String getFunctionName() {
        return functionName;
    }


    public List<String> functionArgumentsToStringList(String args) {
        List<String> lst = new ArrayList<>();
        String str = String.valueOf(args);

        while (!str.isEmpty()) {
            str = str.replaceFirst("^[(),]+", "");

            if (!str.isEmpty()) {
                lst.add(findArgument(str));
                str = str.replace(lst.getLast(), "");
            }
        }

        return lst;
    }

    private String findArgument(String subArg) {
        int index = subArg.indexOf(",");

        if (index == -1) {
            return subArg.replaceAll("\\)", "");
        }

        String str = subArg.substring(0, index);
        if (isFirstArgIsVar(str)) {
            return str;
        }

        int counter = 1, ind = 0;

        while (counter != 0) {
            if (subArg.charAt(ind) == '(') {
                counter++;
            }
            else if (subArg.charAt(ind) == ')') {
                counter--;
            }
            ind++;
        }
        return subArg.substring(0, ind - 1);

    }

    public boolean isFirstArgIsVar(String arg) {
        char ch = Character.toUpperCase(arg.charAt(0));

        if (ch != 'Z' && ch != 'Y' && ch != 'X') {
            return false;
        }
        if (ch == 'Y'&& arg.length() == 1) {
            return true;
        }

        try {
            String str = arg.substring(1);
            int i = Integer.parseInt(str);
            return true;
        }
        catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            return false;
        }
    }

    public List<String> extractXVariables() {
        return Arrays.stream(functionArguments.split("[^a-zA-Z0-9]+"))
                .filter(part -> part.matches("[xX]\\d+"))
                .distinct()
                .collect(Collectors.toList());
    }

}