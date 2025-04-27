package org.example.Language;

import org.example.Support.Support;

/*
 * Statement to access variables from the local variable space of the current function runner.
 */
public class StatementVariable extends StatementBase
{
    private final String variableName;

    //  Constructor
    public StatementVariable(String variableName)
    {
        this.variableName = variableName;
    }

    /*
     * Get a value from the variable pool, defaulting to 0 if it is undefined.
     */
    @Override
    public int getValue(StatementProgram program, FunctionRunner function)
    {
        Integer integer = function.variables.get(variableName);

        return Support.integerToInt(integer);
    }

    /*
     * Set the provided value to the variable pool. This method also updates a
     * list of variables that have had a value set to them.
     */
    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function)
    {
        function.variables.put(variableName, input);
        //  Update which variables have been set to.
        if (!function.variablesSetTo.contains(variableName))
        {
            function.variablesSetTo.add(variableName);
        }
    }

    @Override
    public boolean isStable()
    {
        return false;
    }
}
