package org.example.Compilation;

import java.util.*;

import org.example.Language.Constants;
import org.example.Support.Support;

/*
 * Temporary object for Statement objects. 
 * TreeStatementElements are created when converting expressions to statements.
 * These manage the children that they have, what they are, and where they originally came from.
 * All for use by the compiler.
 */
class TreeStatementElement
{
    public String token;
    public List<TreeStatementElement> children;
    //  If we are missing parameters, this is how many that we want
    public int parameterCount;
    public TreeStatementElementType type;
    //  Where in the array of tokens this element came from
    public int indexInExpressionTokens;

    public String getToken()
    {
        return token;
    }

    /*
     * Constructor, also responsible for figuring out the type of this element.
     */
    public TreeStatementElement(String token, FunctionDetails[] functionDetails)
    {
        this.token = token;

        //  Default to unknown
        type = TreeStatementElementType.Unknown;

        //  Check the easy ones
        if (token.equals(Constants.emptyElementToken))
        {
            type = TreeStatementElementType.NullType;
        }
        else if (Support.isConstantToken(token))
        {
            type = TreeStatementElementType.Constant;
        }
        else if (Support.stringInArray(token, Constants.soloBuiltins))
        {
            type = TreeStatementElementType.SoloBuiltin;
            parameterCount = 1;
        }
        else if (Support.stringInArray(token, Constants.dualBuiltins))
        {
            type = TreeStatementElementType.DualBuiltin;
            parameterCount = 2;
        }
        else if (Support.stringInArray(token, Constants.intermediateStatements))
        {
            type = TreeStatementElementType.Intermediate;
        }

        //  Check if this is a function
        //  There are times when I pass a null value to functionDetails because I know that it isn't a function
        if (functionDetails != null)
        {
            //  Loop until we have searched all the functions, or we know this is a function
            for (int i = 0; i < functionDetails.length && type != TreeStatementElementType.Function; i++)
            {
                if (functionDetails[i].getFunctionName().equals(token))
                {
                    parameterCount = functionDetails[i].getParameterNames().length;
                    type = TreeStatementElementType.Function;
                }
            }
        }

        //  If it's still unknown, check if it's a variable (which is if its none of the builtin tokens)
        if (type == TreeStatementElementType.Unknown)
        {
            boolean foundMatchingBuiltin = false;

            //  Check the list of all builtin statements to determine if this token is a builtin statement
            foundMatchingBuiltin = Support.stringInArray(token, Constants.builtinStatements);

            //  If this is not a builtin and not a user defined function, then set it as complete
            //  This works because this method is called before I start unsetting the missing parameters flag
            if (!foundMatchingBuiltin && type != TreeStatementElementType.Function)
            {
                type = TreeStatementElementType.Variable;
            }
        }

        children = new ArrayList<>();
    }

    /*
     * Check if this element belongs in the operator stack (infix to postfix
     * conversion)
     */
    public boolean operatorStack()
    {
        return type == TreeStatementElementType.SoloBuiltin || type == TreeStatementElementType.DualBuiltin
                || (type == TreeStatementElementType.Function && parameterCount >= 1)
                || type == TreeStatementElementType.Intermediate;
    }

    /*
     * Checks if this object is lower priority than the one passed as a
     * parameter.
     */
    public boolean isLowerPriorityThan(TreeStatementElement otherElement)
    {
        boolean result;

        //  Brackets are always top priority
        if (type == TreeStatementElementType.Intermediate)
        {
            result = false;
        }
        //  If other element is a function, keep it around, otherwise stuff breaks
        else if (otherElement.parameterCount >= 3)
        {
            result = false;
        }
        //  Finally, check it the old-fashioned way
        else
        {
            result = getPriority() > otherElement.getPriority();
        }

        return result;
    }

    /*
     * Get the priority of this object, higher number is higher priority.
     */
    private int getPriority()
    {
        int result = 0;

        //  If it only has one parameter it's the highest priority
        if (parameterCount == 1)
        {
            result = 3;
        }
        //  Two parameters follow that
        else if (parameterCount == 2)
        {
            result = 2;
        }
        //  Then any more parameters are the next priority
        else
        {
            result = 1;
        }

        return result;
    }

    /*
     * Add an element to the list of children that this element has.
     */
    public void add(TreeStatementElement child)
    {
        children.add(child);
    }

    /*
     * Returns a new tree statement element of the null type.
     */
    public static TreeStatementElement getZeroElement()
    {
        TreeStatementElement element = new TreeStatementElement(Constants.emptyElementToken, null);
        return element;
    }

    /*
     * Returns this object and its children as a string in a tree format.
     */
    public String toString()
    {
        return elementToString(this, 0);
    }

    /* Recursive method to generate a visual structure of the element trees */
    private static String elementToString(TreeStatementElement element, int depth)
    {
        String result = "";

        result += Support.multiplyString(depth, "  ") + element.tokenForDisplay() + "\n";

        for (int i = 0; i < element.children.size(); i++)
        {
            result += elementToString(element.children.get(i), depth + 1);
        }

        return result;
    }

    /*
     * Converts every element in the list to a string, ignores their children
     */
    public static String elementListToString(List<TreeStatementElement> elements)
    {
        String result = "";

        for (int i = 0; i < elements.size(); i++)
        {
            TreeStatementElement element = elements.get(i);
            result += element.tokenForDisplay() + "  ";
        }

        return result;
    }

    /*
     * Returns this objects token, but if its empty it is replaced with some
     * text to indicate that is the case. This is only for viewing the elements.
     */
    public String tokenForDisplay()
    {
        String value = token;
        if (token.isEmpty())
        {
            value = "<EMPTY_TOKEN>";
        }
        return value;
    }
}
