package org.example.Language;

/*
 * Class containing all the constants that are used by Digital Logic.
 * Also contains the first ID to use when assigning IDs to functions.
 */
public class Constants
{
    //  Solo operators
    public static final String notStatement = "!";
    public static final String terminalStatement = "_";
    public static final String[] soloBuiltins = new String[]
    { notStatement, terminalStatement };

    //  Dual operators
    public static final String andStatement = "&";
    public static final String orStatement = "|";
    public static final String accessBitStatement = ":";
    public static final String fileStatement = "/";
    public static final String reflectionStatement = "?";
    public static final String[] dualBuiltins = new String[]
    { andStatement, orStatement, accessBitStatement, fileStatement, reflectionStatement };

    //  Language tokens
    public static final String expressionCenter = "=";
    public static final String lineSeperatorToken = "\n";
    public static final String blockStartStatement = "(";
    public static final String blockEndStatement = ")";
    public static final String commentStatement = "#";
    public static final String commentBlockStatement = "##";
    public static final String functionStart = "<<";
    public static final String functionSeperator = "//";
    public static final String functionEnd = ">>";
    public static final String[] intermediateStatements = new String[]
    { expressionCenter, lineSeperatorToken, blockStartStatement, blockEndStatement, commentStatement,
            commentBlockStatement, functionStart, functionSeperator, functionEnd };

    //  Array of every builtin operator
    public static final String[] builtinStatements = new String[]
    { notStatement, andStatement, orStatement, accessBitStatement, fileStatement, terminalStatement,
            reflectionStatement, expressionCenter, lineSeperatorToken, blockStartStatement, blockEndStatement,
            commentStatement, commentBlockStatement, functionStart, functionSeperator, functionEnd };

    //  Misc tokens
    public static final String emptyElementToken = "";

    //  Function start ID
    public static final int userSpaceFunctionIDs = 1;

    public static String getFileSeperator()
    {
        return System.getProperty("file.separator");
    }
}
