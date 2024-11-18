package org.example.Compilation;
import org.example.Language.Function;

/*
 * Stores a bunch of temporary data about functions during the compilation process.
 * These objects exist alongside the real functions during the compilation process.
 */
class FunctionDetails
{
    private String functionName;
    private String returnName;
    private int ID;
    private String[] paramterNames;
    //  Because a function can be both written to and from, we need seperate get and set sections
    private String[] getTokens;
    private String[] setTokens;

    //  Constructor
    public FunctionDetails(String functionName, String returnName, int ID, String[] paramterNames, String[] getTokens, String[] setTokens)
    {
        this.functionName = functionName;
        this.returnName = returnName;
        this.ID = ID;
        this.paramterNames = paramterNames;
        this.getTokens = getTokens;
        this.setTokens = setTokens;
    }

    /*
     * Creates a real function object with basic information from this function details object
     */
    public Function getFuntionFromDetails()
    {
        return new Function(functionName, paramterNames, returnName, ID);
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

    public String[] getParamterNames()
    {
        return this.paramterNames;
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
        for (int i = 0; i < getParamterNames().length; i++)
        {
            result += getParamterNames()[i] + " ";
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