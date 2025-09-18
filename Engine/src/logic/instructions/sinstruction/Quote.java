package logic.instructions.sinstruction;

import logic.function.Function;
import logic.instructions.Instruction;
import logic.instructions.InstructionData;
import logic.instructions.quoteutil.ExprNode;
import logic.instructions.quoteutil.ParenExprParser;
import logic.label.FixedLabel;
import logic.label.Label;
import logic.variable.Variable;
import program.Program;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Quote extends Instruction implements SyntheticInstruction {
    private final Program root;                 // ה-root לפתרון שמות פונקציות
    private final Function function;            // הפונקציה שמריצים בפועל (לפי functionName)
    private final String functionName;
    private String functionArguments;

    private static final Pattern X_VAR = Pattern.compile("\\b([xyzXYZ]\\w*)\\b");

    // בנאי ציבורי – כשמגיע מ-ProgramLoad / הוראות S
    public Quote(Program program, Variable var, Label label, String quoteName, String arguments) {
        super(program, InstructionData.QUOTE, var, label);
        this.root = program; // בהיעדר מידע אחר, ה-root הוא ה-program שנמסר
        this.functionName = quoteName;
        this.functionArguments = arguments;

        // לפתור פונקציה רק מול ה-root שמחזיק את כל הפונקציות
        this.function = root.getFunctionByName(functionName);

        checkIfVar();
    }

    // בנאי פנימי – לשימוש עבור child-Quotes בשלב ההרכבה
    private Quote(Program context, Variable var, Label label, String quoteName, String arguments, Program root) {
        super(context, InstructionData.QUOTE, var, label);
        this.root = root;
        this.functionName = quoteName;
        this.functionArguments = arguments;
        this.function = root.getFunctionByName(functionName);
        checkIfVar();
    }

    // ---- מהלך ההרצה הכולל: טוען קלטים לפונקציה, פותר הרכבות → מריץ → כותב Y ל-var של האבא ----
    private void getVariablesFromMain() {
        // 1) מיפוי ארגומנטים אל Maps של הפונקציה שמריצים + בניית רשימת קלטים
        List<Long> inputs = putVarsInMaps();
        function.getProgramLoad().loadInputVars(inputs);

        // 2) פירסינג לעץ הקריאה (שם הפונקציה + הארגומנטים המפורשים)
        ParenExprParser parser = new ParenExprParser("(" + functionName + "," + functionArguments + ")");
        List<ExprNode> exprs = parser.parseProgram();

        // 3) החלפת קריאות פנימיות במשתני עבודה טריים והזנת תוצאות הביניים ל-outer
        AtomicBoolean changed = new AtomicBoolean(false);
        ArrayList<Long> tmp = new ArrayList<>();
        Quote flattened = calculateNodeExp(exprs.getFirst().getValue(), exprs.getFirst().getArgs(), changed, tmp);

        // 4) לאחר שהביטוי "הושטח", מריצים וקובעים את Y בקונטקסט החיצוני (this)
        long y = flattened.calcQuoteValue();
        this.setVarValueInMap(y); // כתיבה למפות של ההוראה החיצונית (האבא)
    }

    /**
     * מחליפה רקורסיבית קריאות פנימיות במשתנים טריים. עבור כל קריאה פנימית:
     * - מריצים אותה בקונטקסט של הפונקציה המתאימה (child)
     * - כותבים את תוצאת הביניים (Y של הילד) אל המפות של ה-outer תחת המשתנה הטרי
     * - מחליפים את הצומת לקריאה בשם המשתנה
     */
    private Quote calculateNodeExp(String value,
                                   ArrayList<ExprNode> exps,
                                   AtomicBoolean changed,
                                   ArrayList<Long> lst) {
        Quote quote = this;

        // אם כל הארגומנטים אטומיים – אין קריאות פנימיות ברמה זו, נחזיר את ה-Quote הנוכחי
        boolean allAtoms = exps.stream().allMatch(ExprNode::isAtom);
        if (allAtoms) {
            // כאן אין יצירת Quote חדש; ההרצה בפועל תתבצע לאחר שהעץ כולו הוחלק
            changed.set(false);
            return quote;
        }

        // יש קריאות פנימיות: מטיילים עליהן, פותרים אותן ומחליפים למשתנים
        for (ListIterator<ExprNode> it = exps.listIterator(); it.hasNext(); ) {
            ExprNode exp = it.next();
            if (!exp.isCall()) continue;

            AtomicBoolean childChanged = new AtomicBoolean(false);
            Quote childQuote = calculateNodeExp(exp.getValue(), exp.getArgs(), childChanged, lst);

            if (childChanged.get()) {
                // כבר טופל למטה, אין צורך כאן – אבל נשאיר את הדגל
                changed.set(true);
            } else {
                // זהו עלה: קריאה עם אטומים – מקצים משתנה טרי להחלפה
                Variable tmpVar = function.getExpanderExecute().getFreshWork();

                // נבנה Quote שמייצג את הקריאה הזו child(value, args)
                String funcArgs = exp.getArgs().stream()
                        .map(ExprNode::getValue)
                        .collect(Collectors.joining(","));
                Quote inner = new Quote(function, tmpVar, FixedLabel.EMPTY, exp.getValue(), funcArgs, this.root);

                // 1) למפות של הפונקציה הפנימית: למפות ארגומנטים ולטעון inputs
                List<Long> innerInputs = inner.putVarsInMaps();
                inner.function.getProgramLoad().loadInputVars(innerInputs);

                // 2) להריץ את הילד ולקבל את Y שלו
                long innerY = inner.calcQuoteValue();

                // 3) לכתוב את התוצאה לקונטקסט החיצוני (this.getProgram())
                Program outer = this.getProgram();
                char p = Character.toLowerCase(tmpVar.getRepresentation().charAt(0));
                switch (p) {
                    case 'x': outer.setXVariablesToMap(tmpVar, innerY); break;
                    case 'z': outer.setZVariablesToMap(tmpVar, innerY); break;
                    case 'y': outer.setY(innerY); break;
                    default:  outer.setZVariablesToMap(tmpVar, innerY); break;
                }

                // 4) להחליף את הקריאה בצומת של שם המשתנה
                it.set(new ExprNode(tmpVar.getRepresentation()));
                changed.set(true);
            }
        }

        return quote;
    }

    /**
     * סורקת את מחרוזת הארגומנטים, מעדכנת את Maps של הפונקציה function
     * ובונה רשימת קלטים ל-loadInputVars. לא מבצעת load כאן!
     */
    private List<Long> putVarsInMaps() {
        List<Long> lst = new ArrayList<>();
        if (functionArguments == null || functionArguments.isBlank()) return lst;

        for (String part : functionArguments.split(",")) {
            if (part == null) continue;
            String s = part.trim();
            if (s.isEmpty()) continue;

            Matcher m = X_VAR.matcher(s);
            while (m.find()) {
                String varName = m.group(1); // למשל x1, y, z3
                Long value = getValueFromMapByString(varName);
                if (value == null) value = 0L;
                lst.add(value);

                char prefix = Character.toLowerCase(varName.charAt(0));
                switch (prefix) {
                    case 'x': {
                        Variable key = getVarFromMapByString(varName);
                        if (key != null) function.setXVariablesToMap(key, value);
                        break;
                    }
                    case 'y': {
                        function.setY(value);
                        break;
                    }
                    case 'z': {
                        Variable key = getVarFromMapByString(varName);
                        if (key != null) function.setZVariablesToMap(key, value);
                        break;
                    }
                    default:
                        // מתעלמים
                }
            }
        }
        return lst;
    }

    private void checkIfVar() {
        if (functionArguments == null || functionArguments.isBlank()) return;

        for (String val : functionArguments.split(",")) {
            if (val == null) continue;

            Matcher m = X_VAR.matcher(val);
            while (m.find()) {
                String varName = m.group(); // למשל x1, x2, z3, y
                super.getProgram().setValueToMapsByString(varName);
            }
        }
    }

    // === הרצה בפועל של הפונקציה (אחרי שהביטוי הוחלק) ===
    public long calcQuotationValue() {
        return function.getProgramExecutor().run();
    }

    public Long calcQuoteValue() {
        return function.getProgramExecutor().run();
    }

    @Override
    public int calcCycles() {
        return 5 + function.getProgramExecutor().getSumOfCycles();
    }

    // === נקודת הכניסה של ההוראה ===
    @Override
    public Label calculateInstruction() {
        getVariablesFromMain(); // מטפל בכל הזרימה, כולל כתיבת Y ל-var של הפקודה
        return FixedLabel.EMPTY;
    }

    @Override
    public String getCommand() {
        return super.getVar().getRepresentation() + " <- " + "(" + functionName + ", " + functionArguments + ")";
    }

    public String getFunctionArguments() {
        return functionArguments;
    }

    // עוזרים (בהנחה שקיימים ב-Instruction/Program; שומרים חתימות כמו אצלך)
    public Variable getVarKeyFromMapByString(String val) {
        return function.getKeyFromMapsByString(val);
    }

    public List<Instruction> getInstructionsFromFunction() {
        return function.getInstructionList();
    }

    public String getFunctionName() {
        return functionName;
    }
}