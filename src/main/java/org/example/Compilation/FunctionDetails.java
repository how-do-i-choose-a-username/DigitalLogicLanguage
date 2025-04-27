package org.example.Compilation;
import org.example.Language.Function;

/*
 * Stores a bunch of temporary data about functions during the compilation process.
 * These objects exist alongside the real functions during the compilation process.
 */
class FunctionDetails
{
    private final String functionName;
    private final String returnName;
    private int ID;
    private final String[] parameterNames;
    //  Because a function can be both written to and from, we need separate get and set sections
    private final String[] getTokens;
    private final String[] setTokens;

    //  Constructor
    public FunctionDetails(String functionName, String returnName, int ID, String[] parameterNames, String[] getTokens, String[] setTokens)
    {
        this.functionName = functionName;
        this.returnName = returnName;
        this.ID = ID;
        this.parameterNames = parameterNames;
        this.getTokens = getTokens;
        this.setTokens = setTokens;
    }

    /*
     * Creates a real function object with basic information from this function details object
     */
    public Function getFunctionFromDetails()
    {
        return new Function(functionName, parameterNames, returnName, ID);
    }

    public String getFunctionName()
    {
        return this.functionName;
    }

    public String getReturnName()
    {
        return this.returnName;
    }

    public int getID()
    {
        return this.ID;
    }

    public void setID(int ID)
    {
        this.ID = ID;
    }

    public String[] getParameterNames()
    {
        return this.parameterNames;
    }

    public String[] getGetTokens()
    {
        return getTokens;
    }

    public String[] getSetTokens()
    {
        return setTokens;
    }

    public String toString()
    {
        String result = "";
        result += getFunctionName() + " ID " + getID() + "\n";
        result += "Return name " + getReturnName() + "\n";
        result += "Parameters ";
        for (int i = 0; i < getParameterNames().length; i++)
        {
            result += getParameterNames()[i] + " ";
        }
        result += "\nGetter tokens ";
        for (int i = 0; i < getGetTokens().length; i++)
        {
            result += getGetTokens()[i] + " ";
        }
        result += "\nSetter tokens ";
        for (int i = 0; i < getSetTokens().length; i++)
        {
            result += getSetTokens()[i] + " ";
        }
        result += "\n";
        return result;
    }
}