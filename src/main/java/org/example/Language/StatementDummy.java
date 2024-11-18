package org.example.Language;

/*
 * Simple statement used for testing. It contains a persistent variable I can put values into to test other code.
 */
public class StatementDummy extends StatementBase
{
    private int value;

    @Override
    public int getValue(StatementProgram program, FunctionRunner function)
    {
        return value;
    }

    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function)
    {
        value = input;
    }

    @Override
    public boolean isStable()
    {
        return false;
    }
}
