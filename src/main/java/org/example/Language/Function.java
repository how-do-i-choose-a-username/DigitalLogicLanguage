package org.example.Language;

import java.io.Serializable;

/*
 * Object containing an executable function.
 * Has methods to execute the code the function contains when either getting or setting to it.
 */
public class Function implements Serializable
{
    public final String symbol;
    public final String[] parameterNames;
    public final String returnName;
    public Expression[] expressionsGet;
    public Expression[] expressionsSet;
    public int functionID;
    public int referenceCount = 0;

    //  Constructor
    public Function(String symbol, String[] parameterNames, String returnName, int functionID)
    {
        this.symbol = symbol;
        this.parameterNames = parameterNames;
        this.returnName = returnName;
        this.functionID = functionID;
    }

    //  Constructor
    public Function(String symbol, String[] parameterNames, String returnName, Expression[] expressionsGet,
            Expression[] expressionsSet)
    {
        this.symbol = symbol;
        this.parameterNames = parameterNames;
        this.returnName = returnName;
        this.expressionsGet = expressionsGet;
        this.expressionsSet = expressionsSet;
    }

    /*
     * Execute all the expressions this function contains when getting a value
     * from this function.
     */
    public void executeGet(StatementProgram program, FunctionRunner function) throws Exception
    {
        for (int i = 0; i < expressionsGet.length; i++)
        {
            expressionsGet[i].evaluate(program, function);
        }
    }

    /*
     * Execute all the expressions this function contains when setting a value
     * to this function.
     */
    public void executeSet(StatementProgram program, FunctionRunner function) throws Exception
    {
        for (int i = 0; i < expressionsSet.length; i++)
        {
            expressionsSet[i].evaluate(program, function);
        }
    }
}
