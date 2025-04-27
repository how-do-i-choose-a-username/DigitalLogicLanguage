package org.example.Language;

/*
 * Statement with a constant value. Cannot be set to.
 */
public class StatementConstant extends StatementBase
{
    private final int value;

    public StatementConstant(int value)
    {
        this.value = value;
    }

    @Override
    public int getValue(StatementProgram program, FunctionRunner function)
    {
        return value;
    }

    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception
    {
        throw new Exception("Invalid operation, cannot set a constant value");
    }

    @Override
    public boolean isStable()
    {
        return true;
    }
}
