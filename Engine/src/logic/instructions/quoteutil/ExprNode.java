package logic.instructions.quoteutil;

import java.util.*;

public class ExprNode {
    enum Type { ATOM, CALL, EMPTY }

    final Type type;
    final String value;
    final ArrayList<ExprNode> args; // תמיד ArrayList בר-שינוי

    // EMPTY
    private ExprNode(Type t) {
        this.type = t;
        this.value = null;
        this.args = new ArrayList<>();
    }

    // ATOM
    public ExprNode(String atom) {
        this.type = Type.ATOM;
        this.value = atom;
        this.args = new ArrayList<>();
    }

    // CALL
    public ExprNode(String head, List<ExprNode> args) {
        this.type = Type.CALL;
        this.value = head;
        // העתק הגנתי ל-ArrayList כדי להבטיח בר-שינוי ולהתנתק מהרשימה שהתקבלה
        this.args = new ArrayList<>(args != null ? args : Collections.emptyList());
    }

    public static ExprNode empty() { return new ExprNode(Type.EMPTY); }

    public boolean isAtom()  { return type == Type.ATOM; }
    public boolean isCall()  { return type == Type.CALL; }
    public boolean isEmpty() { return type == Type.EMPTY; }

    public Type getType() { return type; }
    public String getValue() { return value; }

    // מאחזרים את ה-ArrayList עצמו (שימו לב: שינוי הרשימה ישנה את הצומת)
    public ArrayList<ExprNode> getArgs() { return args; }

    // אופציונלי: עזר לשינוי נוח
    public ExprNode addArg(ExprNode n) { this.args.add(n); return this; }
    public ExprNode removeArg(int idx) { this.args.remove(idx); return this; }
    public ExprNode clearArgs() { this.args.clear(); return this; }

    @Override
    public String toString() {
        if (isEmpty()) return "()";
        if (isAtom())  return value;
        StringBuilder sb = new StringBuilder();
        sb.append('(').append(value);
        for (ExprNode a : args) sb.append(',').append(a.toString());
        sb.append(')');
        return sb.toString();
    }

    public String prettyPrint() { return prettyPrint(0); }
    private String prettyPrint(int indent) {
        String pad = "  ".repeat(indent);
        if (isEmpty()) return pad + "()";
        if (isAtom())  return pad + value;
        StringBuilder sb = new StringBuilder();
        sb.append(pad).append(value).append('(').append('\n');
        for (int i = 0; i < args.size(); i++) {
            sb.append(args.get(i).prettyPrint(indent + 1));
            if (i < args.size() - 1) sb.append(',');
            sb.append('\n');
        }
        sb.append(pad).append(')');
        return sb.toString();
    }
}
