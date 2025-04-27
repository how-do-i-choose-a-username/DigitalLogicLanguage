package org.example.Language;

import java.util.*;

import org.example.Support.Support;

/*
 * This statement is the main entry point into a Digital Logic program. 
 * It can be called like any other statement (currently unused)
 * It contains a few methods used by the reflection operator.
 * It works by simply containing a StatementFunction for the mainline of the program.
 */
public class StatementProgram extends StatementBase
{
    //  This is an array of all functions, so that they can be called via reflection
    private final Function[] functions;
    //  This is a dictionary of global variables, so they can be accessed via reflection
    private Dictionary<Integer, Integer> globalVariables;
    //  This is the function that is run (the mainline)
    private StatementFunction mainlineFunction;

    //  Constructor
    public StatementProgram(Function function, StatementBase[] parameters, Function[] functions)
    {
        mainlineFunction = new StatementFunction(function, parameters);

        //  Initialise the program specific stuff
        this.functions = functions;
    }

    /*
     * Method used by the reflection operator to set to a function via its ID.
     * The variableStart parameter is where to start getting parameters for this function from the global variable space.
     */
    public void setToFunction(int functionID, int value, int variableStart) throws Exception
    {
        Function function = getFunctionWithID(functionID);

        int[] calculatedParameters = getVariableArray(variableStart, function.parameterNames.length);

        FunctionRunner runner = new FunctionRunner();
        //  This shouldn't break it, I have a check for null values
        runner.setToFunction(function, calculatedParameters, value, this);
    }

    /* 
     * Method used by the reflection operator to get from a function via its ID.
     * The variableStart parameter is where to start getting parameters for this function from the global variable space.
     */
    //  When calculating a function via reflection, one of the specified parameters is where to start looking for variables
    public int getFromFunction(int functionID, int variableStart) throws Exception
    {
        Function function = getFunctionWithID(functionID);

        int[] calculatedParameters = getVariableArray(variableStart, function.parameterNames.length);

        FunctionRunner runner = new FunctionRunner();
        Integer result = runner.executeFunctionObject(function, calculatedParameters, this);

        return Support.integerToInt(result);
    }

    /*
     * Method which returns a function from the provided functionID.
     */
    private Function getFunctionWithID(int functionID) throws Exception
    {
        Function result = null;
        for (Function function : functions)
        {
            if (function.functionID == functionID)
            {
                result = function;
            }
        }
        if (result == null)
        {
            String message = "Tried to retrieve a function with an invalid ID " + functionID + "\nAvailable IDs are ";
            for (int i = 0; i < functions.length; i++)
            {
                message += functions[i].functionID + " ";
            }
            throw new RunerException(message);
        }
        return result;
    }

    /*
     * Get an array of variables from the global variable space to use as parameters for the functions being called via reflection.
     */
    private int[] getVariableArray(int startIndex, int count)
    {
        int[] calculatedParameters = new int[count];

        //  Get the variables that we were told to get
        for (int i = 0; i < calculatedParameters.length; i++)
        {
            calculatedParameters[i] = getFromVariable(startIndex + i);
        }

        return calculatedParameters;
    }

    /*
     * Set to a global variable at ID. Used by reflection operator.
     */
    public void setToVariable(int variableID, int value)
    {
        globalVariables.put(variableID, value);
    }

    /*
     * Get a value from the global variables, used by reflection operator.
     */
    public int getFromVariable(int variableID)
    {
        return Support.integerToInt(globalVariables.get(variableID));
    }

    /*
     * Method used to call this program, returns an Integer object which may be null.
     * Initialises this statement, then calls the function contained by this program statement.
     */
    public Integer getValueObject() throws Exception
    {
        globalVariables = new Hashtable<>();
        return mainlineFunction.getValueObject(this, null);
    }

    /*
     * Get a value from this program, like any other statement.
     * Wraps the getValueObject method to ensure this program has its own global variable space.
     * This method is currently unused.
     */
    @Override
    public int getValue(StatementProgram program, FunctionRunner function) throws Exception
    {
        return Support.integerToInt(getValueObject());
    }

    /*
     * Cannot set a value to a program, so throw an error if it ever happens.
     */
    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception
    {
        throw new RunerException("Cannot set a value to a program");
    }

    @Override
    public boolean isStable()
    {
        return false;
    }

    @Override
    public StatementBase[] getStatements()
    {
        return new StatementBase[]
        { mainlineFunction };
    }

    @Override
    public void setStatements(StatementBase[] statements)
    {
        if (statements.length == 1 && statements[0] instanceof StatementFunction)
        {
            mainlineFunction = (StatementFunction) statements[0];
        }
        else
        {
            throw new Error("Invalid set statements to statement program");
        }
    }
}
