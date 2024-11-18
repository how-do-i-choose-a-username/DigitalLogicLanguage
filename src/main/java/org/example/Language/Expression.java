package org.example.Language;

import java.io.Serializable;

/*
 * Class which contains a single executable expression, which is basically a single line of code.
 */
public class Expression implements Serializable
{
    private StatementBase toSet;
    private StatementBase toGet;

    //  Constructor
    public Expression(StatementBase toSet, StatementBase toGet)
    {
        this.toSet = toSet;
        this.toGet = toGet;
    }

    /*
     * Evaluate this expression. Gets a value from the get statement, then sets
     * it to the set statement. Thats all this method does.
     */
    public void evaluate(StatementProgram program, FunctionRunner function) throws Exception
    {
        toSet.setValue(toGet.getValue(program, function), program, function);
    }
}
