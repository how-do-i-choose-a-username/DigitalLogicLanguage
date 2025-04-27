package org.example.Language;

import org.example.Support.Support;

/*
 * This statement is to execute a function. It does so by creating a function runner for that function, and calculating parameters.
 */
public class StatementFunction extends StatementBase
{
    private final Function ownedFunction;
    private StatementBase[] parameters;

    //  Constructor
    public StatementFunction(Function function, StatementBase[] parameters)
    {
        this.ownedFunction = function;
        this.parameters = parameters;
    }

    /*
     * Get an integer values from the function runner. This may return null
     * (which is helpful).
     */
    public Integer getValueObject(StatementProgram program, FunctionRunner function) throws Exception
    {
        int[] calculatedParameters = calculateParameters(program, function);

        //  Execute the function after giving it the calculated parameters
        FunctionRunner runner = new FunctionRunner();
        Integer result = runner.executeFunctionObject(ownedFunction, calculatedParameters, program);

        return result;
    }

    /*
     * Get a value object, and convert it to an int value.
     */
    @Override
    public int getValue(StatementProgram program, FunctionRunner function) throws Exception
    {
        //  This conversion method is necessary because the object returned may be null
        return Support.integerToInt(getValueObject(program, function));
    }

    /*
     * Set a value to the function.
     */
    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception
    {
        int[] calculatedParameters = calculateParameters(program, function);

        FunctionRunner runner = new FunctionRunner();
        runner.setToFunction(this.ownedFunction, calculatedParameters, input, program);

        //  If we have been provided with parameters at all  
        if (parameters != null)
        {
            //  For every parameter, pass along the value setting to the statement, if necessary
            for (int i = 0; i < ownedFunction.parameterNames.length; i++)
            {
                //  If this parameter was set to
                if (runner.variablesSetTo.contains(ownedFunction.parameterNames[i]))
                {
                    //  Get the parameter, and set it to the corresponding statement from the parameters
                    int param = Support.integerToInt(runner.variables.get(ownedFunction.parameterNames[i]));
                    parameters[i].setValue(param, program, function);
                }
            }
        }
    }

    /*
     * Calculate the parameters provided to this function, simply by executing
     * their statements.
     */
    private int[] calculateParameters(StatementProgram program, FunctionRunner function) throws Exception
    {
        //  Calculate the statements being used as parameters
        int[] calculatedParameters = new int[parameters.length];

        for (int i = 0; i < parameters.length; i++)
        {
            calculatedParameters[i] = parameters[i].getValue(program, function);
        }

        return calculatedParameters;
    }

    @Override
    public boolean isStable()
    {
        return false;
    }

    //  This is passing by reference, which could become a problem
    @Override
    public StatementBase[] getStatements()
    {
        return parameters;
    }

    @Override
    public void setStatements(StatementBase[] statements)
    {
        if (statements.length != parameters.length)
        {
            throw new Error("Was provided with parameters of the wrong length!");
        }

        parameters = statements;
    }
}
