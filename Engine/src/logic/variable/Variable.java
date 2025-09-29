package logic.variable;

public interface Variable {
    VariableType getType();
    String getRepresentation();
    int getVariableIndex();

    Variable RESULT = new VariableImpl(VariableType.RESULT, 0);
}
