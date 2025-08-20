package Program;

public class ProgramLoadException extends RuntimeException {
    public ProgramLoadException(String message) { super(message); }
    public ProgramLoadException(String message, Throwable cause) { super(message, cause); }
}
