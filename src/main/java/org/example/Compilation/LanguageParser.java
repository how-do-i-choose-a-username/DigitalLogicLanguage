package org.example.Compilation;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

import org.example.Language.*;
import org.example.Support.*;


/*
 * This class of static methods is responsible for the core of the compilation process. 
 * It parses and tokenises a file, then interprets those tokens to produce an executable data structure.
 */
public class LanguageParser
{
    /*
     * Parses the file at the given file path, expects a file of the .dl type.
     * Uses the default flags. Returns a Statement that can be executed to run
     * the parsed program
     */
    public static StatementProgram parseFile(String fileName) throws Exception
    {
        return parseFile(new FlagManager(fileName));
    }

    /*
     * Uses the provided flags to compile a program, and then return a statement
     * which can be executed.
     */
    public static StatementProgram parseFile(FlagManager parameters) throws Exception
    {
        //  List of tokens in the provided file
        List<String> tokens = tokeniseFile(parameters);

        //  Run some basic checks
        if (parameters.isCompileTimeChecking())
        {
            checkTokens(tokens);
        }

        //  Construct the function details for the mainline
        String fileNameEnd = Paths.get(parameters.getFileToLoad()).getFileName().toString();
        Debugger.logMessage("File name for return value", fileNameEnd);
        FunctionDetails mainlineFuncDetails = new FunctionDetails("", fileNameEnd, 0, new String[0],
                tokens.toArray(new String[0]), new String[0]);

        //  Get the function details from the tokens
        List<FunctionDetails> funcDetailsList = findFunctionTokens(tokens);
        //  Insert the mainline function at the start of the list
        funcDetailsList.add(0, mainlineFuncDetails);

        FunctionDetails[] functionDetails = funcDetailsList.toArray(new FunctionDetails[0]);
        Function[] functions = new Function[functionDetails.length];

        //  Partially construct all the actual functions
        for (int i = 0; i < functions.length; i++)
        {
            functions[i] = functionDetails[i].getFuntionFromDetails();
        }

        //  Foreach function (Which includes the mainline)
        //  Evaluate the tokens, and convert them to expressions

        //  Take the function data for each function, and finalise the function with the actual code it will use
        for (int i = 0; i < functionDetails.length; i++)
        {
            FunctionDetails funcDetail = functionDetails[i];

            ExpressionDetails[] getters = tokensToExpressions(funcDetail.getGetTokens());
            ExpressionDetails[] setters = tokensToExpressions(funcDetail.getSetTokens());

            functions[i].expressionsGet = expressionDetailsToExpressions(getters, functionDetails, functions,
                    parameters);
            functions[i].expressionsSet = expressionDetailsToExpressions(setters, functionDetails, functions,
                    parameters);
        }

        //  Set the mainline to have 1 references, so that it never gets deleted
        functions[0].referenceCount = 1;

        if (parameters.isStripUnusedCode())
        {
            //  Trim the functions which are never referenced
            functions = trimUnusedFunctions(functions);
        }
        Debugger.logMessage("Function count", "" + functions.length);

        if (parameters.isCompileTimeChecking())
        {
            checkFunctionParameters(functions);
        }

        //  Finalise, and return a statement that can be executed, the first function is the mainline
        StatementProgram returnValue = new StatementProgram(functions[0], new StatementBase[0], functions);

        return returnValue;
    }

    /*
     * Tokenise the file at the given file path, including new lines because
     * otherwise we cant tell where they go
     */
    private static List<String> tokeniseFile(FlagManager flagManager)
    {
        ArrayList<String> tokens = new ArrayList<String>(100);

        try
        {
            File file = new File(flagManager.getFileToLoad());
            FileInputStream fileInputStream = new FileInputStream(file);
            Scanner sc = new Scanner(fileInputStream);

            boolean foundCommentBlock = false;

            //  For each line in the file, split it up by spaces, and add the token to the list
            //  Also remove comment tokens, and the actual comment
            while (sc.hasNextLine())
            {
                boolean foundLineComment = false;

                String nextLine = sc.nextLine();

                if (flagManager.isCompileTimeChecking())
                {
                    if (nextLine.contains("\t"))
                    {
                        Debugger.logWarning("Tab character",
                                "Found a tab character in the source file. This may or may not be intentional, "
                                        + "and has the potential to break things. Found in the line: '" + nextLine
                                        + "'");
                    }
                }

                for (String string : nextLine.split(" "))
                {
                    if (string.equals(Constants.commentStatement))
                    {
                        foundLineComment = true;
                    }
                    else if (string.equals(Constants.commentBlockStatement))
                    {
                        foundCommentBlock = !foundCommentBlock;
                    }
                    else if (!foundLineComment && !foundCommentBlock && !string.isBlank())
                    {
                        tokens.add(string);
                    }
                }

                //  Guarantee that the last token in the file is a new line
                //  This is depended on by other functions in this file
                if ((tokens.size() >= 1 && !tokens.get(tokens.size() - 1).equals(Constants.lineSeperatorToken))
                        || tokens.size() == 0)
                {
                    tokens.add(Constants.lineSeperatorToken);
                }
            }

            sc.close();
            fileInputStream.close();

        } catch (IOException exception)
        {
            System.out.println("An error was encountered loading the file from disk");
            System.out.println(exception.getMessage());
        }

        return tokens;
    }

    /*
     * Finds every function in the token list, and builds a function details
     * object for each.
     */
    private static List<FunctionDetails> findFunctionTokens(List<String> tokens) throws CompilerException
    {
        List<FunctionDetails> functionDetails = new ArrayList<FunctionDetails>();

        for (int i = 0; i < tokens.size(); i++)
        {
            String currentToken = tokens.get(i);

            if (currentToken.equals(Constants.functionStart))
            {
                functionDetails.add(findFunction(tokens, i));
            }
        }

        Debugger.logMessage("About to assign all function IDs", "");

        assignFunctionIDs(functionDetails, Constants.userSpaceFunctionIDs);

        return functionDetails;
    }

    /*
     * This function builds a single function details object given a token list,
     * and the index of a function starter token
     */
    private static FunctionDetails findFunction(List<String> tokens, int start) throws CompilerException
    {
        enum TokenTarget
        {
            Parameter, GetTokens, SetTokens
        }

        String functionName = "";
        //  Default to 0, deal with this later
        int ID = 0;
        List<String> parameterNames = new ArrayList<>();
        List<String> getTokens = new ArrayList<>();
        List<String> setTokens = new ArrayList<>();
        String returnName = "";
        //  This is to store the offset of the parameters from the function start
        //  This can vary based on if the function has been assigned an ID manually

        //  As we loop through the tokens, 'target' defines what to do with them
        TokenTarget target = TokenTarget.Parameter;
        //  Loop through every token, starting at the first parameter (which is the return name) 
        //  until the end of the function, or the end of the tokens
        for (int i = start + 1; i < tokens.size() && !tokens.get(i).equals(Constants.functionEnd); i++)
        {
            String currentToken = tokens.get(i);

            //  When we get to a function break, switch where we are putting the tokens
            if (currentToken.equals(Constants.functionSeperator))
            {
                if (target == TokenTarget.Parameter)
                {
                    target = TokenTarget.GetTokens;
                }
                else if (target == TokenTarget.GetTokens)
                {
                    target = TokenTarget.SetTokens;
                }
            }
            //  If its a normal token, just keep it, but put it in the correct place
            else
            {
                switch (target)
                {
                case GetTokens:
                    getTokens.add(currentToken);
                    break;
                case Parameter:
                    if (Support.isConstantToken(currentToken))
                    {
                        if (ID != 0)
                        {
                            throw new CompilerException("Found a second constant token in the parameters list");
                        }
                        //  Check that the constant is within the first two elements of the parameters
                        else if (i <= start + 2)
                        {
                            ID = Support.bitsToInt(currentToken);
                        }
                        else
                        {
                            throw new CompilerException(
                                    "Found a constant too far into the paramter list of a function.");
                        }
                    }
                    //  Check if its blank to remove any newline tokens
                    else if (!currentToken.isBlank())
                    {
                        //  If the function name is not set, and the ID is still 0 then the token is the function name
                        if (functionName.isEmpty() && ID == 0)
                        {
                            functionName = currentToken;
                        }
                        //  If its not a constant or the function name, its a parameter
                        else
                        {
                            parameterNames.add(currentToken);
                        }
                    }
                    break;
                case SetTokens:
                    setTokens.add(currentToken);
                    break;
                default:
                    //  The Name case is unhandled here, it should never be encountered in this switch statement
                    throw new Error("Function finder got into an unaceptable state.");
                }
            }
        }

        //  The first parameter is actually the return name
        if (parameterNames.size() >= 1)
        {
            returnName = parameterNames.remove(0);
        }

        //  Build the actual function details object here
        FunctionDetails functionDetail = new FunctionDetails(functionName, returnName, ID,
                parameterNames.toArray(new String[0]), getTokens.toArray(new String[0]),
                setTokens.toArray(new String[0]));

        return functionDetail;
    }

    /*
     * Assigns a unique ID to each function in the list. If the function has an
     * ID of 0, it needs to be assigned and ID, if it is not 0 then it was
     * manually specified by the programmer. The mainline function has not been
     * added yet, so dosnt need worrying about.
     */
    private static void assignFunctionIDs(List<FunctionDetails> functionDetails, int startID)
    {
        int nextID = startID;
        Debugger.logMessage("About to assign IDs", "Function details size " + functionDetails.size());

        for (int i = 0; i < functionDetails.size(); i++)
        {
            FunctionDetails funcDetails = functionDetails.get(i);
            //  Only assing an ID if it has an ID of 0, which means it hasnt been given an ID yet
            if (funcDetails.getID() == 0)
            {
                nextID = nextFreeID(functionDetails, nextID);
                funcDetails.setID(nextID);
                Debugger.logMessage("Assigned ID to function",
                        "Assigned an ID of " + nextID + " to the function '" + funcDetails.getFunctionName() + "'");
            }
        }
    }

    /*
     * Returns the next free ID in the function details list
     */
    private static int nextFreeID(List<FunctionDetails> functionDetails, int currentID)
    {
        while (functionHasID(functionDetails, currentID))
        {
            currentID += 1;
        }

        return currentID;
    }

    /*
     * Checks if the function list has the provided ID already assigned
     */
    private static boolean functionHasID(List<FunctionDetails> functionDetails, int currentID)
    {
        boolean result = false;

        for (int i = 0; i < functionDetails.size() && !result; i++)
        {
            result = functionDetails.get(i).getID() == currentID;
        }

        return result;
    }

    /*
     * Convert the token array into an array of expression details. This
     * involves finding the expressions, and then which tokens are getters or
     * setters.
     */
    private static ExpressionDetails[] tokensToExpressions(String[] tokens)
    {
        //  The first step is to figure out how to iterate over expressions
        //  Expressions can be on their own line, but also in brackets (at which case, brackets start the 'line')
        //  If we find a function, just skip the whole thing

        List<ExpressionDetails> expressions = new ArrayList<>();

        //  Iterate on our own, that way we can simply jump as far as we need to
        for (int i = 0; i < tokens.length;)
        {
            //  4 types of tokens we can encounter
            //  Not an expression, so thats things like functions and their definition
            //  Start block, which is directly before an expression, but its tricky because it dosnt always mean that its an expression
            //  End block, which ends an expression
            //  normal token, which we want to actually put into the expression

            //  If we find a new line token, we dont care about it, just go the next line
            if (tokens[i].equals(Constants.lineSeperatorToken))
            {
                i += 1;
            }
            //  If we find the start of an expression block, turn it into an expression
            else if (multiExpressionLine(tokens, i))
            {
                //Debugger.logMessage("Line type", "Dealing with a multi expression line");
                //  Find the closing bracket index, and then generate expression from the statements in that space
                int blockCloseIndex = findBlockCloseIndex(tokens, i);
                expressions.add(expressionFromTokens(tokens, i + 1, blockCloseIndex));

                //  Go to the closing block index, then add one to go to the next token
                i = blockCloseIndex + 1;
            }
            //  If we found the start of a function, skip the whole thing
            else if (tokens[i].equals(Constants.functionStart))
            {
                i = findNextIndexOfToken(tokens, Constants.functionEnd, i) + 1;
            }
            //  We found a normal expression, what a suprise
            else
            {
                //  Find the next new line, that is the end of the current expression
                int expressionEnd = findNextIndexOfToken(tokens, Constants.lineSeperatorToken, i);
                expressions.add(expressionFromTokens(tokens, i, expressionEnd));

                i = expressionEnd;
            }

            //  Output all the generated expressions, if debugging
            if (Debugger.isDebugging())
            {
                String expressionsString = "";
                for (ExpressionDetails expressionDetails : expressions)
                {
                    expressionsString += expressionDetails.toString() + "\n";
                }
                Debugger.logMessage("Expressions", expressionsString);
            }
        }

        //  Cast a list to an array, but its a bit weird because Java
        return expressions.toArray(new ExpressionDetails[0]);
    }

    /*
     * This recursive method is to check that this is a line with an expression
     * wrapped in block operators, and not just a block at the start of the
     * line. If a line starts with a block starter, then make sure the entire
     * line is enclosed with blocks.
     */
    private static boolean multiExpressionLine(String[] tokens, int startIndex)
    {
        boolean result = false;

        //  If it starts with a block start, then it could be a multi expression line
        if (tokens[startIndex].equals(Constants.blockStartStatement))
        {
            result = multiExpressionLineInternal(tokens, startIndex);
        }

        return result;
    }

    /*
     * Recursive method to check if an expression is enclosed with block tokens.
     */
    private static boolean multiExpressionLineInternal(String[] tokens, int startIndex)
    {
        boolean result = false;
        int index = findBlockCloseIndex(tokens, startIndex);

        //  First, check that the next token exists. If the next element is outside of the array, it still worked
        if (index + 1 >= tokens.length)
        {
            result = true;
        }
        //  Check the next token. If its a block start, re run this process. If its a newline, end this process. If its somthing else, fail
        else if (tokens[index + 1].equals(Constants.blockStartStatement))
        {
            result = multiExpressionLineInternal(tokens, index + 1);
        }
        else if (tokens[index + 1].equals(Constants.lineSeperatorToken))
        {
            result = true;
        }

        return result;
    }

    /*
     * Finds the next index of the specified token, defaulting to tokens length.
     * This is because the newline tokens end at the end of the file.
     */
    private static int findNextIndexOfToken(String[] tokens, String target, int startIndex)
    {
        int result = tokens.length;

        //  Loop through every token, or until result is updated
        for (int i = startIndex; i < tokens.length && result == tokens.length; i++)
        {
            if (tokens[i].equals(target))
            {
                result = i;
            }
        }

        return result;
    }

    /*
     * Fetches a sub array of tokens, start inclusive and end exclusive. Then
     * iterates over it, and makes an expression details object
     */
    private static ExpressionDetails expressionFromTokens(String[] tokens, int startIndex, int endIndex)
    {
        List<String> leftSideTokens = new ArrayList<>();
        List<String> rightSideTokens = new ArrayList<>();
        boolean encounteredCenter = false;

        for (int i = startIndex; i < endIndex; i++)
        {
            if (tokens[i].equals(Constants.expressionCenter))
            {
                encounteredCenter = true;
            }
            else if (encounteredCenter)
            {
                rightSideTokens.add(tokens[i]);
            }
            else
            {
                leftSideTokens.add(tokens[i]);
            }
        }

        //  If we didnt find the center of the expression (no '=' sign), then move all the tokens from the LHS to the RHS
        if (!encounteredCenter)
        {
            rightSideTokens = leftSideTokens;
            leftSideTokens = new ArrayList<>();
        }

        return new ExpressionDetails(leftSideTokens.toArray(new String[0]), rightSideTokens.toArray(new String[0]));
    }

    /* Convert an array of expression details into real expressions */
    private static Expression[] expressionDetailsToExpressions(ExpressionDetails[] expressionDetails,
            FunctionDetails[] functionDetails, Function[] functions, FlagManager flags) throws Exception
    {
        Expression[] expressions = new Expression[expressionDetails.length];

        for (int i = 0; i < expressionDetails.length; i++)
        {
            expressions[i] = expressionDetailToExpression(expressionDetails[i], functionDetails, functions, flags);
        }

        return expressions;
    }

    /* Converts a single expression detail into an expression */
    private static Expression expressionDetailToExpression(ExpressionDetails expressionDetail,
            FunctionDetails[] functionDetails, Function[] functions, FlagManager flags) throws Exception
    {
        //  Convert each side individually
        StatementBase set = tokensToStatement(expressionDetail.setterTokens, functionDetails, functions, flags);
        StatementBase get = tokensToStatement(expressionDetail.getterTokens, functionDetails, functions, flags);

        //  Optimise the getter code, if set to do so
        if (flags.isOptimiseCode())
        {
            get = optimiseStatements(get);
        }

        return new Expression(set, get);
    }

    /*
     * Optimise statements to reduce the number needed to run at execution time.
     * Very basic optimisation method, simply checks if the value of a statement
     * is unchanging, and if it is, pre calculate it.
     */
    private static StatementBase optimiseStatements(StatementBase statement)
    {
        //  If this statment is stable, calculate a value and replace this with a constant
        //  If this is not stable, recursivly call this method on its children

        Debugger.logMessage("Optimising", "Optimising the statement " + statement);

        StatementBase result = statement;

        if (statement instanceof StatementConstant)
        {
            //  Do nothing, theres no point optimisng a constant, its already optimal
        }
        //  If this statement is stable, calculate it now
        else if (statement.isStable())
        {
            //  This shouldnt fail, if it does I have a problem
            try
            {
                //  I should be fine passing null, null. Those values are only used by unstable statements
                int value = statement.getValue(null, null);
                result = new StatementConstant(value);
                Debugger.logMessage("Optimised statement", "Optimised the statement " + statement + " to " + value);
            } catch (Exception e)
            {
                Debugger.logWarning("Failed to optimise the statement " + statement, e.toString());
            }
        }
        //  If this statement is unstable, call this method on its children
        else
        {
            StatementBase[] toOptimise = statement.getStatements();

            if (toOptimise != null)
            {
                for (int i = 0; i < toOptimise.length; i++)
                {
                    toOptimise[i] = optimiseStatements(toOptimise[i]);
                }

                statement.setStatements(toOptimise);
            }
        }

        return result;
    }

    /*
     * Converts an array of tokens into a single Statement object that can be
     * executed. This is used on each side of an expression, for example.
     */
    private static StatementBase tokensToStatement(String[] tokens, FunctionDetails[] functionDetails,
            Function[] functions, FlagManager flags) throws Exception
    {
        List<TreeStatementElement> elements = new ArrayList<>(
                Arrays.asList(tokensToTreeElements(tokens, functionDetails)));

        //  I need a stack for upcoming operators, and a queue for numbers and stuff.

        //  Stack of all the operators being dealt with
        List<TreeStatementElement> operatorsStack = new ArrayList<>();
        //  The postfix expression being built
        List<TreeStatementElement> postfixExpression = new ArrayList<>();
        //  Last index when we directly added a value to the postfix expression
        int lastNumberAddition = -10;

        if (Debugger.isDebugging())
        {
            Debugger.logMessage("Evaluating tokens", TreeStatementElement.elementListToString(elements));
        }

        //  Remove and handle each element of the list, one at a time
        for (int j = 0; j < elements.size(); j++)
        {
            TreeStatementElement element = elements.get(j);
            element.indexInExpressionTokens = j;

            //  If its a block start, put it on the operators
            if (element.token.equals(Constants.blockStartStatement))
            {
                operatorsStack.add(element);
            }
            //  If we encounter the start of a function, skip it
            else if (element.token.equals(Constants.functionStart))
            {
                j = findNextIndexOfToken(tokens, Constants.functionEnd, j);
            }
            //  If its a block end, take all the operators off the operator stack until we find a closing bracket, then remove it
            else if (element.token.equals(Constants.blockEndStatement))
            {
                for (int i = operatorsStack.size() - 1; i >= 0
                        && !operatorsStack.get(i).token.equals(Constants.blockStartStatement); i--)
                {
                    moveOperatorToPostfix(operatorsStack.remove(i), postfixExpression);
                }

                //  Delete the block start
                if (operatorsStack.get(operatorsStack.size() - 1).token.equals(Constants.blockStartStatement))
                {
                    operatorsStack.remove(operatorsStack.size() - 1);
                }
                else
                {
                    throw new CompilerException("Failed to remove block start at expected position");
                }
            }
            //  If it belongs on the operator stack, put it there (takes 1 or more parameters, or is a block)
            else if (element.operatorStack())
            {
                //  If a dual operator has an assumed 0 on the left, handle that case. 
                //  If we dont do this the value on the right becomes the first value
                if (element.parameterCount == 2 && postfixExpression.size() == 0)
                {
                    postfixExpression.add(TreeStatementElement.getZeroElement());
                }

                //  While the last element in the operator stack has a lower priority, move it to the postfix expression
                while (operatorsStack.size() > 0
                        && operatorsStack.get(operatorsStack.size() - 1).isLowerPriorityThan(element))
                {
                    TreeStatementElement operator = operatorsStack.remove(operatorsStack.size() - 1);
                    moveOperatorToPostfix(operator, postfixExpression);
                }

                operatorsStack.add(element);
            }
            //  Otherwise just put it into the numbers
            else
            {
                //  If the last element we added was also a number, we are in a functions parameters
                //  Now we need to handle those cases, where there are operators in a functions parameters
                if (lastNumberAddition == j - 1)
                {
                    //  While we have operators to deal with, move them to the expression until we find a function
                    while (operatorsStack.size() > 0
                            && operatorsStack.get(operatorsStack.size() - 1).parameterCount <= 2)
                    {
                        //  Take the last element off of the operatorsStack, and move it to the postfix expression
                        moveOperatorToPostfix(operatorsStack.remove(operatorsStack.size() - 1), postfixExpression);
                    }
                }

                postfixExpression.add(element);
                lastNumberAddition = j;
            }

            debugTokensToStatement(elements, operatorsStack, postfixExpression);
        }

        //  Put any remaining operators into the postfix expression
        for (int i = operatorsStack.size() - 1; i >= 0; i--)
        {
            moveOperatorToPostfix(operatorsStack.remove(i), postfixExpression);
        }

        debugTokensToStatement(elements, operatorsStack, postfixExpression);

        if (postfixExpression.size() > 1)
        {
            throw new CompilerException("Too many elements in postfix expression after evaluation");
        }

        //  (or something that works out to be no expression)
        else if (postfixExpression.size() == 0)
        {
            //Debugger.logMessage("Postfix size 0",
            //        "Postfix expression has a size of 0, it probably shouldnt be like this. There are some cases where it shold be though");
            postfixExpression.add(TreeStatementElement.getZeroElement());
        }
        else if (flags.isCompileTimeChecking())
        {
            checkParesedExpression(postfixExpression.get(0), elements);
        }

        return executionTreeToStatements(postfixExpression.get(0), functions);
    }

    /*
     * Moves the provided operator to postfix, taking the appropriate amount of
     * elements then filling out the rest with empty elements.
     */
    private static void moveOperatorToPostfix(TreeStatementElement operator, List<TreeStatementElement> postfix)
            throws Exception
    {
        if (operator.type == TreeStatementElementType.Intermediate)
        {
            throw new CompilerException("Got given an intermediate statement to put in postfix, this shouldnt happen");
        }

        //  If we only want a single parameter, and the latest postfix came before this operator, add an empty element to the operator
        if (operator.parameterCount == 1 && postfix.size() > 0
                && postfix.get(postfix.size() - 1).indexInExpressionTokens < operator.indexInExpressionTokens)
        {
            operator.add(TreeStatementElement.getZeroElement());
        }
        else
        {
            //  Remove elements until we have enough or have run out
            for (int i = 0; i < operator.parameterCount && postfix.size() >= 1; i++)
            {
                operator.children.add(0, postfix.remove(postfix.size() - 1));
            }
        }

        //  Add an appropriate amount of zero elements, as a quick fix
        while (operator.children.size() < operator.parameterCount)
        {
            operator.add(TreeStatementElement.getZeroElement());
        }

        postfix.add(operator);
    }

    /*
     * Recursive method to check that the tokens an operator or function has
     * came from the correct place. Eg. Functions and solo operators only have
     * children from the right. If it takes two children, its a bit more
     * complicated because the first child can come from either side. The and
     * operator wants a child either side, but some dual functions may want both
     * on the right
     */
    private static void checkParesedExpression(TreeStatementElement element, List<TreeStatementElement> elements)
            throws Exception
    {
        //  If we are looking at the first parameter of a dual operation, which must be on the left side of the operator
        boolean firstDualParameter = element.children.size() == 2;

        //  Check each child of this object
        for (TreeStatementElement child : element.children)
        {
            if (!child.token.equals(Constants.emptyElementToken))
            {
                boolean rightHandSide = rightHandSide(element, child, elements);
                if (rightHandSide && firstDualParameter)
                {
                    throw new CompilerException("Found the token '" + child.token
                            + "' which came from the right hand side of the operator '" + element.token
                            + "' when it should come from the left.");
                }
                //  Throw an error if a child did not come from the right side the element
                else if (!rightHandSide)
                {
                    //  Handle the case where if it takes two parameters, the first can be on the left
                    if (firstDualParameter)
                    {
                        firstDualParameter = false;
                    }
                    else
                    {
                        throw new CompilerException(
                                "Found the token '" + child.token + "' which came from the left side of the operator '"
                                        + element.token + "' when it should come from the right");
                    }
                }
            }

            //  Only recursivly check the operators, other values dont have children
            if (child.parameterCount != 0)
            {
                checkParesedExpression(child, elements);
            }

            //  We are no longer looking at the first parameter, so turn it off
            firstDualParameter = false;
        }
    }

    /* Check if toFind is on the right side of pivot in the elements list */
    private static boolean rightHandSide(TreeStatementElement pivot, TreeStatementElement toFind,
            List<TreeStatementElement> elements)
    {
        //  Result
        boolean isOnRightHandSide = false;

        //  Empty elements are the values which are not in the original expression, but are assumed to exist
        if (toFind.token.equals(Constants.emptyElementToken))
        {
            isOnRightHandSide = true;
        }
        else if (toFind.indexInExpressionTokens > pivot.indexInExpressionTokens)
        {
            isOnRightHandSide = true;
        }

        return isOnRightHandSide;
    }

    /*
     * Recursively convert a TreeStatementElement and all its children into a
     * statement that can be executed
     */
    private static StatementBase executionTreeToStatements(TreeStatementElement element, Function[] functions)
    {
        StatementBase result;

        //  Depending on what the element is, convert it to the corresponding statement type
        switch (element.type)
        {
        case Constant:
            result = new StatementConstant(Support.bitsToInt(element.token));
            break;
        case DualBuiltin:
            result = new StatementDualBuiltin(element.token,
                    executionTreeToStatements(element.children.get(0), functions),
                    executionTreeToStatements(element.children.get(1), functions));
            break;
        case Function:
            TreeStatementElement parametersParent = element;

            //  If the only child this object has is a block, 
            //  then that block has the parameters to this function, so open the block
            if (element.children.size() == 1)
            {
                if (element.children.get(0).type == TreeStatementElementType.Intermediate)
                {
                    parametersParent = element.children.get(0);
                }
            }

            //  Convert all the parameters to this function call into statements
            StatementBase[] statementBases = new StatementBase[parametersParent.children.size()];
            for (int i = 0; i < statementBases.length; i++)
            {
                statementBases[i] = executionTreeToStatements(parametersParent.children.get(i), functions);
            }

            //  Find the correct function from the list
            Function func = null;
            for (int i = 0; i < functions.length; i++)
            {
                if (functions[i].symbol.equals(element.token))
                {
                    func = functions[i];
                }
            }

            //  Keep a record of how many times this function is used
            func.referenceCount += 1;

            //  Then build the fuction statement from that information
            result = new StatementFunction(func, statementBases);
            break;
        case Intermediate:
            //  If an element has an Intermediate type, then ignore it and get its child
            result = executionTreeToStatements(element.children.get(0), functions);
            break;
        case SoloBuiltin:
            result = new StatementSoloBuiltin(element.token,
                    executionTreeToStatements(element.children.get(0), functions));
            break;
        case Variable:
            result = new StatementVariable(element.token);
            break;
        case NullType:
            result = new StatementNull();
            break;
        case Unkown:
            throw new Error("Encountered an element with an unknown type " + element.token);
        default:
            throw new Error(
                    "Encountered an element with an unhandled or unspecified type, it may need to be added as a type option");

        }

        return result;
    }

    /*
     * Convert the token string array into an array of TreeStatementElements,
     * which can be used to build a tree structure
     */
    private static TreeStatementElement[] tokensToTreeElements(String[] tokens, FunctionDetails[] functionDetails)
    {
        TreeStatementElement[] treeElements = new TreeStatementElement[tokens.length];

        for (int i = 0; i < tokens.length; i++)
        {
            treeElements[i] = new TreeStatementElement(tokens[i], functionDetails);
        }

        return treeElements;
    }

    /*
     * This method finds the index of the closing block token which matches the
     * one at the startIndex.
     */
    private static int findBlockCloseIndex(String[] tokens, int startIndex)
    {
        int closeIndex = -1;
        int bracketDepth = 0;
        //  Loop until we reach the end of the array or have found the closing block
        for (int i = startIndex; i < tokens.length && closeIndex == -1; i++)
        {
            //  If its a block start increase depth
            if (tokens[i].equals(Constants.blockStartStatement))
            {
                bracketDepth += 1;
            }
            //  If its a block close decrease depth
            else if (tokens[i].equals(Constants.blockEndStatement))
            {
                bracketDepth -= 1;
            }

            if (bracketDepth == 0)
            {
                closeIndex = i;
            }
        }

        return closeIndex;
    }

    /*
     * Trims unreferenced functions from the array. Each function has a counter
     * of how many times it is referenced in the code. If this method is called,
     * it finds these and removes them from the array of all functions.
     */
    private static Function[] trimUnusedFunctions(Function[] functions)
    {
        List<Function> usedFunctions = new ArrayList<>();
        //  Collect all the functions that have references
        for (int i = 0; i < functions.length; i++)
        {
            if (functions[i].referenceCount > 0)
            {
                usedFunctions.add(functions[i]);
            }
        }

        return usedFunctions.toArray(new Function[0]);
    }

    /*
     * This method checks the function parameters of all functions for overlaps
     * with other functions, or reserved tokens. It is able to be disabled via
     * the Flag manager.
     */
    private static void checkFunctionParameters(Function[] functions) throws CompilerException
    {
        //  For every function
        for (int i = 0; i < functions.length; i++)
        {
            //  For every function again
            for (int j = 0; j < functions.length; j++)
            {
                //  If they are not the same function, check that the name and parameter are not the same
                if (i != j)
                {
                    if (functions[i].symbol.equals(functions[j].symbol) && !functions[i].symbol.isBlank())
                    {
                        throw new CompilerException(
                                "Found two seperate functions with the same name, big problem, fix that");
                    }
                    else if (functions[i].functionID == functions[j].functionID)
                    {
                        throw new CompilerException(
                                "Found two seperate functions with the same ID assigned, they were '"
                                        + functions[i].symbol + "' and '" + functions[j].symbol + "' and an ID of "
                                        + functions[i].functionID);
                    }
                }

                //  Check that the parameter names do not conflict with other function names
                for (String parameter : functions[i].parameterNames)
                {
                    if (parameter.equals(functions[j].symbol))
                    {
                        throw new CompilerException("Function '" + functions[i].symbol + "' has the parameter '"
                                + parameter + "' which clashes with the name of the function '" + functions[j].symbol
                                + "'");
                    }
                }
            }

            //  Check the parameters of each function
            for (String parameter : functions[i].parameterNames)
            {
                if (parameter.equals(functions[i].returnName))
                {
                    throw new CompilerException("The parameter '" + parameter + "' from function '"
                            + functions[i].symbol + "' clashes with the functions return token");
                }
                else if (Support.stringInArray(parameter, Constants.builtinStatements))
                {
                    throw new CompilerException("The parameter '" + parameter + "' from function '"
                            + functions[i].symbol + "' clashes with a builtin token");
                }
                else if (Support.isConstantToken(parameter))
                {
                    throw new CompilerException("The parameter '" + parameter + "' from function '"
                            + functions[i].symbol + "' is a constant value");
                }

                //  Check that there are not duplicated parameter names
                for (String parameterSecond : functions[i].parameterNames)
                {
                    if (parameter != parameterSecond && parameter.equals(parameterSecond))
                    {
                        throw new CompilerException("The parameter '" + parameter + "' from function '"
                                + functions[i].symbol + "' clashes with another parameter to the same function");
                    }
                }
            }
        }
    }

    /*
     * Run some basic checks on the parsed tokens, mainly that paired tokens
     * have a pair.
     */
    private static void checkTokens(List<String> tokens) throws CompilerException
    {
        //  Check that all tokens that go in pairs, have a pair somewhere
        if (!equalCountOfTokens(tokens, Constants.blockStartStatement, Constants.blockEndStatement))
        {
            throw new CompilerException("Unmatching number of block tokens in file to parse");
        }
        if (!equalCountOfTokens(tokens, Constants.functionStart, Constants.functionEnd))
        {
            throw new CompilerException("Unmatching number of function start/stop tokens");
        }
        if (countTokens(tokens, Constants.commentBlockStatement) % 2 != 0)
        {
            throw new CompilerException("Not all block comments have a corresponding block close token");
        }

        for (String token : tokens)
        {
            if (Support.isNumeric(token) && !Support.isConstantToken(token))
            {
                Debugger.logWarning("Numerical token found", "Found the token '" + token
                        + "' which is numeric but not a constant value. This is allowed, but may not be intentional.");
            }
        }
    }

    /*
     * A method used for debugging which prints out the state of the infix to
     * postfix conversion.
     */
    private static void debugTokensToStatement(List<TreeStatementElement> elements,
            List<TreeStatementElement> operatorsStack, List<TreeStatementElement> postfixExpression)
    {
        if (Debugger.isDebugging())
        {
            String output = "Elements: " + TreeStatementElement.elementListToString(elements) + "\n";
            for (int i = 0; i < postfixExpression.size(); i++)
            {
                output += "Postfix part " + i + "\n" + postfixExpression.get(i).toString();
            }
            output += "Operators: " + TreeStatementElement.elementListToString(operatorsStack);

            Debugger.logMessage("Working on postfix expression", output);

            Debugger.pause();
        }
    }

    /*
     * Checks that both of the provided tokens have the same count in the tokens
     * list.
     */
    private static boolean equalCountOfTokens(List<String> tokens, String tokenA, String tokenB)
    {
        return countTokens(tokens, tokenA) == countTokens(tokens, tokenB);
    }

    /*
     * Count how many times a given token appears in the tokens list.
     */
    private static int countTokens(List<String> tokens, String target)
    {
        int count = 0;

        for (String string : tokens)
        {
            if (string.equals(target))
            {
                count += 1;
            }
        }

        return count;
    }
}
