package org.example.Language;

import java.util.*;

import org.example.Support.Support;

/*
 * This class is responsible for running functions.
 * It does this by managing the local variables for that function.
 * It also provides setup and wrapper methods for functions.
 * A new function runner must be created per function call.
 */
public class FunctionRunner
{
    //  This is accessed by the statements that are called because of this function
    public Dictionary<String, Integer> variables;
    //  This is used to lookup which variables have been set to while running the program
    public List<String> variablesSetTo;

    /*
     * Run the provided function, returning an Integer object. This method is
     * responsible for calculating any parameters.
     */
    public Integer executeFunctionObject(Function function, int[] parameterValues, StatementProgram program)
            throws Exception
    {
        //  Initalise the dictionary, using the parameters passed into the function
        variables = new Hashtable<String, Integer>();
        variablesSetTo = new ArrayList<>();
        for (int i = 0; i < parameterValues.length && i < function.parameterNames.length; i++)
        {
            variables.put(function.parameterNames[i], parameterValues[i]);
        }

        //  Run all the expressions held by a function
        function.executeGet(program, this);

        return variables.get(function.returnName);
    }

    /*
     * Run the provided function and return an int value.
     */
    public int executeFunction(Function function, int[] parameterValues, StatementProgram program) throws Exception
    {
        Integer result = executeFunctionObject(function, parameterValues, program);

        return Support.integerToInt(result);
    }

    /*
     * Sets a value to the provided function, also initalises parameters. When
     * the function has finished running, any of the parameters which had a
     * value set to them have their current value set to the statement which
     * initially provided a value for that statement.
     */
    public void setToFunction(Function function, int[] parameterValues, int valueBeingSet, StatementProgram program) throws Exception
    {
        //  Initalise the dictionary, using the parameters passed into the function
        variables = new Hashtable<String, Integer>();
        variablesSetTo = new ArrayList<>();
        for (int i = 0; i < parameterValues.length && i < function.parameterNames.length; i++)
        {
            variables.put(function.parameterNames[i], parameterValues[i]);
        }
        variables.put(function.returnName, valueBeingSet);

        //  Run all the expressions held by a function
        function.executeSet(program, this);
    }
}
