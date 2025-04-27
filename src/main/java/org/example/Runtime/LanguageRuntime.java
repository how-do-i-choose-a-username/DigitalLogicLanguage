package org.example.Runtime;

import org.example.Compilation.*;
import org.example.Language.*;
import org.example.Support.*;

/*
 * This class is responsible for running Digital Logic programs for the user, it's called via the command line.
 */
public class LanguageRuntime
{
    /*
     * Entry point for the language runtime. This function is wrapped by the one
     * in Runtime.java. If the command line had parameters provided, use those,
     * otherwise output a message.
     */
    public static void main(String[] args)
    {
        if (args.length >= 1)
        {
            runProgram(new FlagManager(args));
        }
        else
        {
            printMessage(
                    "Please include a file name to run. This must always be the first parameter. Any desired arguments can follow this. For more information, see the readme and the language specification.");
        }
    }

    /*
     * Main method to run a program using the provided command line flags. Calls
     * the methods to compile, run, and output results.
     */
    public static void runProgram(FlagManager flags)
    {
        Integer result = null;
        if (flags.getWhatToDo() != CompilerMode.Automatic)
        {
            try
            {
                long loadStartTime = System.nanoTime();
                StatementProgram statementProgram = loadProgram(flags);
                long loadEndTime = System.nanoTime();

                long loadDuration = (loadEndTime - loadStartTime) / 1000000;

                if (flags.isShowTimings())
                {
                    printMessage("The program took " + loadDuration + " milliseconds to compile");
                }

                //  Save compiled version
                if (flags.getWhatToDo() == CompilerMode.Compile)
                {
                    saveToCompiled(statementProgram, flags);
                }

                //  Run if that's what we want to do
                if (flags.getWhatToDo() == CompilerMode.RunFile && statementProgram != null)
                {
                    long runStartTime = System.nanoTime();
                    //  Then actually run it
                    result = runStatement(statementProgram);
                    long runEndTime = System.nanoTime();

                    long runDuration = (runEndTime - runStartTime) / 1000000;

                    if (flags.isShowTimings())
                    {
                        printMessage("The program took " + runDuration + " milliseconds to run");
                    }

                    outputResults(result, flags);
                }
            } catch (Exception e)
            {
                printMessage("Failed to run the requested program\n");
                e.printStackTrace();
            }
        }
        else
        {
            printMessage("Automatic compilation is not implemented, please manually compile and run the program.");
        }
    }

    /*
     * Method to load the program in the way specified in the flags.
     */
    private static StatementProgram loadProgram(FlagManager flags)
    {
        StatementProgram statementProgram = null;
        //  If we have a source file, (either by manual override, or detecting it) load that source file
        if ((flags.getFileToLoad().endsWith(LanguageConstants.sourceFileExtension)
                && flags.getFileTypeBeingRead() == FileTypeBeingRead.DetectType)
                || flags.getFileTypeBeingRead() == FileTypeBeingRead.Source)
        {
            statementProgram = loadFromSource(flags);
        }
        //  If we have a compiled file, load it
        else if ((flags.getFileToLoad().endsWith(LanguageConstants.compiledFileExtension)
                && flags.getFileTypeBeingRead() == FileTypeBeingRead.DetectType)
                || flags.getFileTypeBeingRead() == FileTypeBeingRead.Compiled)
        {
            statementProgram = loadFromCompiled(flags.getFileToLoad());
        }
        else
        {
            printMessage("Failed to load the specified file. If not using the file extensions '"
                    + LanguageConstants.sourceFileExtension + "' or '" + LanguageConstants.compiledFileExtension
                    + "' please manually specify the file type. But, ideally just use the normal file extensions.");
        }

        return statementProgram;
    }

    /*
     * Load the program from a source file, by telling the language parser to
     * compile it.
     */
    private static StatementProgram loadFromSource(FlagManager flags)
    {
        StatementProgram statementProgram = null;
        try
        {
            Debugger.setDebuggingMode(flags.isEnableDebugging());
            Debugger.setWarningMode(flags.isShowWarnings());

            statementProgram = LanguageParser.parseFile(flags);

            Debugger.defaultSettings();
        } catch (Exception e)
        {
            printMessage("Failed to compile the specified file\n" + e);
        }
        return statementProgram;
    }

    /*
     * Load the program from a compiled file.
     */
    private static StatementProgram loadFromCompiled(String path)
    {
        return (StatementProgram) Support.load(path);
    }

    /*
     * After loading the program, save it to a compiled file.
     */
    private static void saveToCompiled(StatementProgram program, FlagManager flags)
    {
        Support.save(program, sourceToCompiledName(flags.getFileToLoad()));
    }

    /*
     * Figure out the name of the compiled Digital Logic program, given the name
     * of the source file.
     */
    private static String sourceToCompiledName(String sourceName)
    {
        String result = "";
        //  If we have a compiled file name, we don't need to do anything
        if (sourceName.endsWith(LanguageConstants.compiledFileExtension))
        {
            result = sourceName;
        }
        //  If we have a source file extension, remove it and put the compiled one on instead
        else if (sourceName.endsWith(LanguageConstants.sourceFileExtension))
        {
            //  Remove the source file extension
            result = sourceName.substring(0, sourceName.length() - LanguageConstants.sourceFileExtension.length());
            result = result + LanguageConstants.compiledFileExtension;
        }
        //  If we have any other kind of file, just append the compiled file extension
        else
        {
            result = sourceName + LanguageConstants.compiledFileExtension;
        }
        return result;
    }

    /*
     * Output the results from the program, does so in a variety of different
     * ways (int, bits, char).
     */
    private static void outputResults(Integer result, FlagManager flags)
    {
        if (result != null && flags.isShowResults())
        {
            //  Convert the result into a human format (int, bits, and char if applicable)
            String output = "\nResult '" + Integer.toBinaryString(result) + "' '" + result + "'";
            if (Character.isDefined(result))
            {
                output += " '" + Character.toString(result) + "'";
            }
            printMessage(output);
        }
    }

    /*
     * Actually run the loaded Digital Logic program. Has methods to set up and
     * packup the terminal as necessary.
     */
    private static Integer runStatement(StatementProgram statement)
    {
        TerminalHandler.initialiseTerminal();
        Integer result = null;

        //  Add some code to run at shutdown, this is to fix the command line if I force quit the program
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println();
            TerminalHandler.packupTerminal();
        }));

        try
        {
            result = statement.getValueObject();
        } catch (Exception e)
        {
            printMessage("A fatal error occurred while running the requested program " + e);
        }

        //  Also there is this line to re-enable the buffer if the program exits like usual
        TerminalHandler.packupTerminal();

        return result;
    }

    /*
     * Wrapper function for printing messages about this method.
     */
    private static void printMessage(String message)
    {
        System.out.println(message);
    }
}
