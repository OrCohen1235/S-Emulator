package logic.instructions.quoteutil;

import java.util.*;

public class ParenExprParser {
    // ====== Parser ======
    private final String s;
    private int i = 0;

    public ParenExprParser(String input) {
        this.s = input;
    }

    // מחזיר ArrayList
    public ArrayList<ExprNode> parseProgram() {
        ArrayList<ExprNode> nodes = new ArrayList<>();
        skipWs();
        while (!eof()) {
            nodes.add(parseExpr());
            skipWs();
            if (peek() == ',') {
                i++;
                skipWs();
            } else break;
        }
        skipWs();
        if (!eof()) throw err("תווים מיותרים אחרי סוף הביטוי");
        return nodes;
    }

    // Expr := Atom | '()' | '(' ... ')'
    private ExprNode parseExpr() {
        skipWs();
        char c = peek();
        if (c == '(') return parseFromParens();
        String atom = parseSymbol();
        return new ExprNode(atom);
    }

    // תומך גם ב-() ריק וגם בראש + פסיק יתום: (Head,) => קריאה ללא ארגומנטים
    private ExprNode parseFromParens() {
        expect('(');
        skipWs();
        if (peek() == ')') { // סוגריים ריקים
            i++; // consume ')'
            return ExprNode.empty();
        }

        // הפריט הראשון אמור להיות שם הפונקציה כאטום
        ExprNode first = parseExpr();
        skipWs();

        if (!first.isAtom())
            throw err("ראש של קריאה לפונקציה חייב להיות אטום (שם), קיבלתי: " + first);

        ArrayList<ExprNode> args = new ArrayList<>();

        // 1) ')'  -> ללא ארגומנטים
        // 2) ',' ואז מיד ')'  -> פסיק יתום => ללא ארגומנטים
        // 3) ',' ואז Expr ואז אולי עוד ,Expr...
        if (peek() == ')') {
            i++; // סוגרים
            return new ExprNode(first.getValue(), args);
        }

        if (peek() == ',') {
            i++; // consume ','
            skipWs();
            if (peek() == ')') {
                // (Head,) => ללא ארגומנטים
                i++; // consume ')'
                return new ExprNode(first.getValue(), args);
            }
            // אחרת יש ארגומנט ראשון
            args.add(parseExpr());
            skipWs();
            while (peek() == ',') {
                i++;
                skipWs();
                if (peek() == ')')
                    throw err("פסיק מיותר לפני סוגר סוגריים – חסר ארגומנט אחרי פסיק");
                args.add(parseExpr());
                skipWs();
            }
            expect(')');
            return new ExprNode(first.getValue(), args);
        }

        throw err("ציפיתי ל־',' או ')' אחרי ראש קריאה");
    }

    // ====== Lexer helpers ======
    private String parseSymbol() {
        skipWs();
        int start = i;
        while (!eof()) {
            char c = s.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') i++;
            else break;
        }
        if (start == i) throw err("ציפיתי לסמל (אות/ספרה/קו תחתון)");
        return s.substring(start, i);
    }

    private void expect(char wanted) {
        skipWs();
        if (eof() || s.charAt(i) != wanted) throw err("ציפיתי ל־'" + wanted + "'");
        i++;
    }

    private char peek() {
        return eof() ? '\0' : s.charAt(i);
    }

    private boolean eof() {
        return i >= s.length();
    }

    private void skipWs() {
        while (!eof() && Character.isWhitespace(s.charAt(i))) i++;
    }

    private RuntimeException err(String msg) {
        return new RuntimeException(msg + " בעמדה " + i);
    }
}
