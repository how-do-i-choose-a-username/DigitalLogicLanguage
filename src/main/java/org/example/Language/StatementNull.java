package org.example.Language;

/*
 * Simple statement used when a value is unspecified in the source code.
 * Values set to it are ignored, and it always returns 0.
 * This is distinct from a constant 0 because it can actually be set to.
 */
public class StatementNull extends StatementBase
{
    @Override
    public int getValue(StatementProgram program, FunctionRunner function) throws Exception
    {
        return 0;
    }

    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception
    {

    }

    @Override
    public boolean isStable()
    {
        return true;
    }
}
