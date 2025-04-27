package org.example.Language;

/*
 * Base class for the builtin statements, contains the token only.
 */
public class StatementBuiltinBase extends StatementBase
{
    protected String operation;

    @Override
    public int getValue(StatementProgram program, FunctionRunner function) throws Exception
    {
        throw new Error("Called get value on statement builtin base, this should be overridden");
    }

    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception
    {
        throw new Error("Called set value on statement builtin base, this should be overridden");
    }

    @Override
    public boolean isStable()
    {
        throw new Error("Called is stable on statement builtin base, this should be overridden");
    }

    @Override
    public StatementBase[] getStatements()
    {
        throw new Error("Called get statements on statement builtin base, this should be overridden");
    }

    @Override
    public void setStatements(StatementBase[] statements)
    {
        throw new Error("Called set statements on statement builtin base, this should be overridden");
    }
}
