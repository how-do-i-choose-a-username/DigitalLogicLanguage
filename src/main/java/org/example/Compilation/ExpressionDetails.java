package org.example.Compilation;

/*
 * Temporary class used during compilation to store the tokens an expression has.
 * These tokens are later converted into a real expression.
 */
class ExpressionDetails
{
    public final String[] setterTokens;
    public final String[] getterTokens;

    public ExpressionDetails(String[] setterTokens, String[] getterTokens)
    {
        this.setterTokens = setterTokens;
        this.getterTokens = getterTokens;
    }

    public String toString()
    {
        String result = "";
        result += "Setter tokens ";
        for (String string : setterTokens)
        {
            result += string + " ";
        }
        result += "\nGetter tokens ";
        for (String string : getterTokens)
        {
            result += string + " ";
        }
        return result;
    }
}
