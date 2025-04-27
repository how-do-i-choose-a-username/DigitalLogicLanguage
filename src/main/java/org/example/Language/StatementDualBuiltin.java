package org.example.Language;

/*
 * Statement for the dual parameter builtin.
 */
public class StatementDualBuiltin extends StatementBuiltinBase
{
    private StatementBase first;
    private StatementBase second;

    public StatementDualBuiltin(String operation, StatementBase first, StatementBase second)
    {
        this.operation = operation;
        this.first = first;
        this.second = second;
    }

    /*
     * Code to run when getting a value, switch on the operation defined in the
     * builtin base.
     */
    @Override
    public int getValue(StatementProgram program, FunctionRunner function) throws Exception
    {
        int firstInt = first.getValue(program, function);
        int secondInt = second.getValue(program, function);

        int result = 0;

        switch (operation)
        {
        case Constants.orStatement:
            result = firstInt | secondInt;
            break;
        case Constants.andStatement:
            result = firstInt & secondInt;
            break;
        case Constants.accessBitStatement:
            result = bitsOperatorGet(firstInt, secondInt);
            break;
        case Constants.fileStatement:
            System.out.println("Not implemented");
            break;
        case Constants.reflectionStatement:
            //  If a value is not set to the first parameter, reflection works with variables, otherwise it handles functions
            if (firstInt == 0)
            {
                result = program.getFromVariable(secondInt);
            }
            else
            {
                result = program.getFromFunction(secondInt, firstInt);
            }
            break;
        default:
            throw new Error("Encountered an undefined operator as a dual builtin '" + operation + "'");
        }

        return result;
    }

    /*
     * Code to run when setting a value to this statement, switch on the
     * specified operation. Get values from the parameters before setting to
     * them.
     */
    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception
    {
        int firstInt = first.getValue(program, function);
        int secondInt = second.getValue(program, function);

        switch (operation)
        {
        //  If either of its values are the requested one, it's fine, otherwise set the first one to be the input value
        case Constants.orStatement:
            if (firstInt != input && secondInt != input)
            {
                first.setValue(input, program, function);
            }
            break;
        //  If the statement is not the correct value, set its int to be the correct value
        case Constants.andStatement:
            if (firstInt != input)
            {
                first.setValue(input, program, function);
            }
            if (secondInt != input)
            {
                second.setValue(input, program, function);
            }
            break;
        case Constants.accessBitStatement:
            second.setValue(bitsOperatorSet(firstInt, input), program, function);
            break;
        case Constants.fileStatement:
            System.out.println("Not implemented");
            break;
        case Constants.reflectionStatement:
            //  If a value is not set to the first parameter, reflection works with variables, otherwise it handles functions
            if (firstInt == 0)
            {
                program.setToVariable(secondInt, input);
            }
            else
            {
                program.setToFunction(secondInt, input, firstInt);
            }
            break;
        default:
            throw new Error("Encountered an undefined operator as a dual builtin '" + operation + "'");
        }
    }

    /*
     * Method when getting a value from the bits operator, it's a tad
     * complicated. Create a new int Loop over control bits, when a true bit is
     * found, put the corresponding value from the working value into the next
     * free spot on the newly created int Return the newly created int
     */
    private static int bitsOperatorGet(int controlValue, int workingValue)
    {
        int result = 0;
        int indexOfResult = 0;
        //  Loop over every bit in the control value
        for (int i = 0; i < 32; i++)
        {
            //  If the bit at the current index of the control value is true
            if (((1 << i) & controlValue) != 0)
            {
                //  If the bit at the matching index of working value is true
                boolean workingValueIsOn = ((1 << i) & workingValue) != 0;

                //  If the working value is on, set the next bit in result to also be on
                if (workingValueIsOn)
                {
                    result = result | (1 << indexOfResult);
                }

                //  Go to the next index of result
                indexOfResult += 1;
            }
        }
        return result;
    }

    /*
     * Method when setting to a bits operator, basically the reverse of getting
     * from the bits operator. Also complicated. Loop over bits of control
     * value, until we find a true bit When a true bit is found, set the
     * corresponding working bit to that bit & the number of bits into the
     * provided value we are Keep track of where we are in the provided value
     * with a counter
     */
    private int bitsOperatorSet(int controlValue, int settingValue)
    {
        int workingValue = 0;
        int settingIndex = 0;
        for (int i = 0; i < 32; i++)
        {
            //  If the control bit at the specified index is true
            if (((1 << i) & controlValue) != 0)
            {
                //  If the bit however far into the setting value is true
                boolean settingIndexIsTrue = ((1 << settingIndex) & settingValue) != 0;

                //  Set the value in the working index to also be true
                if (settingIndexIsTrue)
                {
                    workingValue = workingValue | (1 << i);
                }

                settingIndex += 1;
            }
        }

        return workingValue;
    }

    @Override
    public boolean isStable()
    {
        return (operation.equals(Constants.accessBitStatement) || operation.equals(Constants.andStatement)
                || operation.equals(Constants.orStatement)) && first.isStable() && second.isStable();
    }

    @Override
    public StatementBase[] getStatements()
    {
        return new StatementBase[]
        { first, second };
    }

    @Override
    public void setStatements(StatementBase[] statements)
    {
        if (statements.length == 2)
        {
            first = statements[0];
            second = statements[1];
        }
        else
        {
            throw new Error("Set the wrong amount of statements to a dual builtin");
        }
    }
}
