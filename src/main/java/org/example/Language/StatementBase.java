package org.example.Language;

import java.io.Serializable;

/*
 * Base statement object. It defines the methods all implementations must have.
 */
public abstract class StatementBase implements Serializable
{
    //  These are used to execute the statements
    public abstract int getValue(StatementProgram program, FunctionRunner function) throws Exception;
    public abstract void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception;
    
    //  This is to check if a statement has the same result every time
    public abstract boolean isStable();

    //  These are used to set or get the child statements a statement has
    //  Default to getting nothing
    public StatementBase[] getStatements()
    {
        return null;
    }
    //  Default to nothing happening when set to
    public void setStatements(StatementBase[] statements)
    {
    }
}