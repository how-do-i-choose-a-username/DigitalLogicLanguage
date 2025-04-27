package org.example.Language;

import org.example.Runtime.TerminalHandler;

/*
 * Statement for the single parameter builtins.
 */
public class StatementSoloBuiltin extends StatementBuiltinBase
{
    private StatementBase first;

    //  Constructor
    public StatementSoloBuiltin(String operation, StatementBase first)
    {
        this.operation = operation;
        this.first = first;
    }

    /*
     * Get a value from this operator, using the provided parameter.
     */
    @Override
    public int getValue(StatementProgram program, FunctionRunner function) throws Exception
    {
        int firstInt = first.getValue(program, function);

        int result = 0;

        switch (operation)
        {
        case Constants.notStatement:
            //  Inbuilt binary not operator
            result = ~firstInt;
            break;
        case Constants.terminalStatement:
            if ((firstInt & 1) == 0)
            {
                result = TerminalHandler.getCharFromTerminal();
            }
            break;
        default:
            throw new Error("Encountered an undefined operator as a solo builtin '" + operation + "'");
        }

        return result;
    }

    /*
     * Set a value to this operator. The parameter is gotten from before it is
     * set to.
     */
    @Override
    public void setValue(int input, StatementProgram program, FunctionRunner function) throws Exception
    {
        int firstInt = first.getValue(program, function);

        switch (operation)
        {
        case Constants.notStatement:
            //  Inbuilt binary not operator
            first.setValue(~input, program, function);
            break;
        case Constants.terminalStatement:
            //  If we can print the input as a char, do so. Use the provided statement as a check for if we shouldnt do this
            if ((firstInt & 2) == 0)
            {
                //  If the third bit is false, print as a character
                if ((firstInt & 4) == 0)
                {
                    //  Check that its printable, with an extra check for 0 because windows is stupid
                    if (Character.isDefined(input) && input != 0)
                    {
                        System.out.print(Character.toString(input));
                    }
                }
                //  Otherwise if its true, print as a bit string
                else
                {
                    System.out.print(Integer.toBinaryString(input));
                }
            }
            break;
        default:
            throw new Error("Encountered an undefined operator as a solo builtin '" + operation + "'");
        }
    }

    @Override
    public boolean isStable()
    {
        return (operation.equals(Constants.notStatement)) && first.isStable();
    }

    @Override
    public StatementBase[] getStatements()
    {
        return new StatementBase[]
        { first };
    }

    @Override
    public void setStatements(StatementBase[] statements)
    {
        if (statements.length == 1)
        {
            first = statements[0];
        }
        else
        {
            throw new Error("Set wrong amount of statements to solo builtin");
        }
    }
}
