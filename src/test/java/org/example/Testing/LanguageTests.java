package org.example.Testing;

import org.example.Language.*;

/*
 * This script is responsible for running all the basic language functionality tests.
 */
class LanguageTests 
{
    /*
     * Method to run all the basic language tests, and catch any exceptions that they throw.
     */
    public static int runBasicTests()
    {
        try
        {
            testBasicStatements();

            testNestedBasicStatements();

            testExpressions();

            testBasicFunction();

            testVariableStatements();

            testComplexFunction();

            testNestedFunction();
        } catch (Exception e)
        {
            String message = "A basic test failed to run";
            TestsRunnerMain.logFailure(message);
            TestsRunnerMain.logErrorFileException(message, e);
        }

        //  This is the number of tests that were run in this file
        return 7;
    }

    /*
     * Test that constants and the basic operators work as expected.
     */
    private static void testBasicStatements() throws Exception
    {
        //  Constant test
        int valueToTest = 101;
        StatementBase constant = new StatementConstant(valueToTest);
        if (constant.getValue(null, null) != valueToTest)
        {
            TestsRunnerMain.logFailure("Constant statement failed");
        }

        //  'Not' statement test
        StatementBase solo = new StatementSoloBuiltin(Constants.notStatement, constant);
        if (solo.getValue(null, null) != ~valueToTest)
        {
            TestsRunnerMain.logFailure("Not operation failed");
        }

        //  'And' test
        int otherTestValue = 3457;
        StatementBase constantTwo = new StatementConstant(otherTestValue);
        StatementBase and = new StatementDualBuiltin(Constants.andStatement, constant, constantTwo);
        if (and.getValue(null, null) != (valueToTest & otherTestValue))
        {
            TestsRunnerMain.logFailure("And operation failed");
        }

        //  'Or' test
        StatementBase or = new StatementDualBuiltin(Constants.orStatement, constant, constantTwo);
        if (or.getValue(null, null) != (valueToTest | otherTestValue))
        {
            TestsRunnerMain.logFailure("Or operation failed");
        }
    }

    /*
     * Test that statements work correctly when nested within each other.
     */
    private static void testNestedBasicStatements() throws Exception
    {
        //  Comments are provided to show the corresponding binary value to the ints provided
        StatementBase constantOne = new StatementConstant(101); //  0000001100101
        StatementBase constantTwo = new StatementConstant(2); //  0000000000010
        StatementBase constantThree = new StatementConstant(4197); //  1000001100101

        StatementBase notConstTwo = new StatementSoloBuiltin(Constants.notStatement, constantTwo); //  1111111111101
        StatementBase constThreeAndNotConstTwo = new StatementDualBuiltin(Constants.andStatement, constantThree,
                notConstTwo); //  1000001100101
        StatementBase constOneAndConstTwo = new StatementDualBuiltin(Constants.andStatement, constantOne, constantTwo); //  0000000000000
        StatementBase orOfTheLastTwoStatements = new StatementDualBuiltin(Constants.orStatement,
                constThreeAndNotConstTwo, constOneAndConstTwo); //  1000001100101

        if (orOfTheLastTwoStatements.getValue(null, null) != constantThree.getValue(null, null))
        {
            TestsRunnerMain.logFailure(
                    "The more complicated nested statement failed to produce the correct answer (or I messed it up, if this is an error, double check the test)");
        }
    }

    /*
     * Test that an expression works.
     */
    private static void testExpressions() throws Exception
    {
        int constValue = 101;
        StatementBase constant = new StatementConstant(constValue);
        StatementBase dummy = new StatementDummy();
        Expression expression = new Expression(dummy, constant);
        expression.evaluate(null, null);
        if (dummy.getValue(null, null) != constValue)
        {
            TestsRunnerMain.logFailure("An expression failed to execute, this may be caused by deeper issues with statements");
        }
    }

    /*
     * Test a basic function, which also uses variables to work properly.
     */
    private static void testBasicFunction() throws Exception
    {
        String returnVariable = "or";
        String[] parameterNames = new String[0];
        int[] parameterValues = new int[0];

        StatementBase constant = new StatementConstant(1000);
        StatementBase constantTwo = new StatementConstant(10000);

        StatementBase and = new StatementDualBuiltin(Constants.andStatement, constant, constantTwo);
        StatementBase or = new StatementDualBuiltin(Constants.orStatement, constant, constantTwo);
        StatementBase not = new StatementSoloBuiltin(Constants.notStatement, constant);

        StatementBase andDummy = new StatementDummy();
        StatementBase notDummy = new StatementDummy();

        StatementBase orVariable = new StatementVariable(returnVariable);

        Expression andExpression = new Expression(andDummy, and);
        Expression orExpression = new Expression(orVariable, or);
        Expression notExpression = new Expression(notDummy, not);

        Expression[] expressions = new Expression[]
        { andExpression, orExpression, notExpression };

        Function function = new Function("TestFunction", parameterNames, returnVariable, expressions, null);

        FunctionRunner runner = new FunctionRunner();

        int result = runner.executeFunction(function, parameterValues, null);

        //  The return variable of the function is the or value because that was one of the expressions earlier
        if (result != or.getValue(null, null))
        {
            TestsRunnerMain.logFailure("Basic function test failed to return the correct value");
        }
    }

    /*
     * Test that variables work, all they need to do is be assigned to and read from.
     */
    private static void testVariableStatements() throws Exception
    {
        String variableName = "variable";
        String otherVariableName = "otherVariable";

        StatementBase getEmpty = new StatementDummy();
        StatementBase getExisting = new StatementDummy();

        StatementBase constant = new StatementConstant(123456);
        StatementBase thatOtherConstant = new StatementConstant(654321);

        StatementBase variable = new StatementVariable(variableName);
        StatementBase otherVariable = new StatementVariable(otherVariableName);

        Expression[] expressions = new Expression[]
        { new Expression(getEmpty, variable), //  Get empty
                new Expression(variable, constant), //  Assign to empty
                new Expression(variable, thatOtherConstant), //  Assign to existing
                new Expression(getExisting, variable), //  Get existing
                new Expression(otherVariable, constant) //  Assign another
        };

        Function function = new Function("TestFunction", new String[0], otherVariableName, expressions, null);
        FunctionRunner runner = new FunctionRunner();
        int result = runner.executeFunction(function, new int[0], null);

        if (getEmpty.getValue(null, null) != 0)
        {
            TestsRunnerMain.logFailure("Failed when accessing a non existent variable, it should return 0 in those cases");
        }

        if (getExisting.getValue(null, null) != thatOtherConstant.getValue(null, null))
        {
            TestsRunnerMain.logFailure("Something failed when getting or setting a variable");
        }

        if (result != constant.getValue(null, null))
        {
            TestsRunnerMain.logFailure("Function failed to return the correct value, this occurred when assigning another variable");
        }
    }

    /*
     * Test a more complicated function that contains multiple expressions and parameters.
     */
    private static void testComplexFunction() throws Exception
    {
        //  Plan is to put one parameter into the dummy, and the other as a return value
        String parameterOne = "param1";
        String parameterTwo = "param2";
        String returnValue = "return";
        String[] parameters = new String[]
        { parameterOne, parameterTwo };

        StatementBase dummy = new StatementDummy();

        StatementBase paramOne = new StatementConstant(123);
        StatementBase paramTwo = new StatementConstant(321);

        StatementBase[] statements = new StatementBase[]
        { paramOne, paramTwo };

        StatementBase getParamOne = new StatementVariable(parameterOne);
        StatementBase getParamTwo = new StatementVariable(parameterTwo);
        StatementBase returnVariable = new StatementVariable(returnValue);

        Expression[] expressions = new Expression[]
        { new Expression(dummy, getParamOne), new Expression(returnVariable, getParamTwo) };

        Function function = new Function("TestFunction", parameters, returnValue, expressions, null);

        StatementBase statementFunction = new StatementFunction(function, statements);

        if (statementFunction.getValue(null, null) != paramTwo.getValue(null, null)
                || dummy.getValue(null, null) != paramOne.getValue(null, null))
        {
            TestsRunnerMain.logFailure("The complex function (executed with a statement) failed");
        }
    }

    /*
     * Test that a function can be correctly called when nested within another function.
     */
    private static void testNestedFunction() throws Exception
    {
        String innerReturn = "return1";
        StatementBase innerConstant = new StatementConstant(2345);
        StatementBase setToReturnVariable = new StatementVariable(innerReturn);
        Expression[] innerExpressions = new Expression[]
        { new Expression(setToReturnVariable, innerConstant) };

        Function innerFunction = new Function("Inner", new String[0], innerReturn, innerExpressions, null);
        StatementBase statementFunction = new StatementFunction(innerFunction, new StatementBase[0]);

        String outerReturn = "return2";
        StatementBase outerSetReturnVariable = new StatementVariable(outerReturn);
        Expression[] outerExpressions = new Expression[]
        { new Expression(outerSetReturnVariable, statementFunction) };

        Function outerFunction = new Function("Outer", new String[0], outerReturn, outerExpressions, null);
        StatementBase outerStatementFunction = new StatementFunction(outerFunction, new StatementBase[0]);

        if (outerStatementFunction.getValue(null, null) != innerConstant.getValue(null, null))
        {
            TestsRunnerMain.logFailure("Nested functions failed to return the correct result");
        }
    }
}
