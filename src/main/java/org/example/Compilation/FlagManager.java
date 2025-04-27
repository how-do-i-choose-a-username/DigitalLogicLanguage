package org.example.Compilation;

import org.example.Support.Support;

/*
 * Class to parse the provided command line arguments, and convert them to usable data for the Language runner.
 */
public class FlagManager
{
    //  The flags and information needed, I can add more as necessary
    private String fileToLoad = ""; //  Name of file to load
    private FileTypeBeingRead fileTypeBeingRead = FileTypeBeingRead.DetectType; //  Override what type of file is being looked at
    private CompilerMode whatToDo = CompilerMode.RunFile; //  Controls whether the runtime compiles or runs the code
    private boolean compileTimeChecking = true; //  Run language checks while compiling, slow things down a bit when run
    private boolean optimiseCode = false; //  Runs some basic optimisations in the code during the compilation process
    private boolean showWarnings = true; //  Whether to show warnings during the compilation process (None thrown yet)
    private boolean stripUnusedCode = false; //  Remove any unused functions (in a simple way)
    private boolean printFlagStates = false; //  Print out all the flags
    private boolean enableDebugging = false; //  Enable debug messages during compilation
    private boolean showResults = true; //  Show results at the end of the program
    private boolean showTimings = false; //  Show how long compilation and run time took
    //  A great flag to add would be the ability to output all the variables from a function when it finishes

    public String getFileToLoad()
    {
        return fileToLoad;
    }

    public FileTypeBeingRead getFileTypeBeingRead()
    {
        return fileTypeBeingRead;
    }

    public CompilerMode getWhatToDo()
    {
        return whatToDo;
    }

    public boolean isCompileTimeChecking()
    {
        return compileTimeChecking;
    }

    public boolean isOptimiseCode()
    {
        return optimiseCode;
    }

    public boolean isShowWarnings()
    {
        return showWarnings;
    }

    public boolean isStripUnusedCode()
    {
        return stripUnusedCode;
    }

    public boolean isPrintFlagStates()
    {
        return printFlagStates;
    }

    public boolean isEnableDebugging()
    {
        return enableDebugging;
    }

    public boolean isShowResults()
    {
        return showResults;
    }

    public boolean isShowTimings()
    {
        return showTimings;
    }

    //  Flag with only the file to load, and default settings
    public FlagManager(String fileToLoad)
    {
        this.fileToLoad = fileToLoad;
    }

    /*
     * Primary constructor for this class, is provided with the command line
     * arguments for the language runner, and converts them to usable data.
     */
    public FlagManager(String[] args)
    {
        for (int i = 0; i < args.length; i++)
        {
            if (isFlag(args[i], FlagConstants.filePathFlag))
            {
                fileToLoad = valueOfNextString(i + 1, args);
            }
            else if (isFlag(args[i], FlagConstants.fileTypeFlag))
            {
                switch (valueOfNextChar(i + 1, args))
                {
                case 'd':
                    fileTypeBeingRead = FileTypeBeingRead.DetectType;
                    break;
                case 's':
                    fileTypeBeingRead = FileTypeBeingRead.Source;
                    break;
                case 'c':
                    fileTypeBeingRead = FileTypeBeingRead.Compiled;
                    break;
                default:
                    logFlagManagerError(
                            "Invalid file type selected, the available options are DetectType, Source and Compiled");
                    break;
                }
            }
            else if (isFlag(args[i], FlagConstants.compilerModeFlag))
            {
                switch (valueOfNextChar(i + 1, args))
                {
                case 'r':
                    whatToDo = CompilerMode.RunFile;
                    break;
                case 'c':
                    whatToDo = CompilerMode.Compile;
                    break;
                case 'a':
                    whatToDo = CompilerMode.Automatic;
                    break;
                default:
                    logFlagManagerError(
                            "Invalid mode selected, the available options are RunFile, Compile and Automatic");
                    break;
                }
            }
            else if (isFlag(args[i], FlagConstants.compilerChecksFlag))
            {
                compileTimeChecking = valueOfNext(i + 1, args, compileTimeChecking);
            }
            else if (isFlag(args[i], FlagConstants.optimiseCodeFlag))
            {
                optimiseCode = valueOfNext(i + 1, args, optimiseCode);
            }
            else if (isFlag(args[i], FlagConstants.silenceWarningsFlag))
            {
                showWarnings = valueOfNext(i + 1, args, showWarnings);
            }
            else if (isFlag(args[i], FlagConstants.stripCodeFlag))
            {
                stripUnusedCode = valueOfNext(i + 1, args, stripUnusedCode);
            }
            else if (isFlag(args[i], FlagConstants.printFlagState))
            {
                printFlagStates = valueOfNext(i + 1, args, printFlagStates);
            }
            else if (isFlag(args[i], FlagConstants.debuggingEnabled))
            {
                enableDebugging = valueOfNext(i + 1, args, enableDebugging);
            }
            else if (isFlag(args[i], FlagConstants.showResults))
            {
                showResults = valueOfNext(i + 1, args, showResults);
            }
            else if (isFlag(args[i], FlagConstants.showTimings))
            {
                showTimings = valueOfNext(i + 1, args, showTimings);
            }
            //  Check if it's a flag, and throw an error if it is, because its unrecognised
            else if (isFlagToken(i, args))
            {
                logFlagManagerError("Encountered the flag '" + args[i] + "' which is unknown.");
            }
            //  Check if the previous element is not a flag, if it's not then this is the file path
            else if (!isFlagToken(i - 1, args))
            {
                fileToLoad = args[i];
            }

            //  If we encounter a token that's the wrong length, throw an error
            if (args[i].startsWith(FlagConstants.smallFlagPrefix) && args[i].length() != 2)
            {
                logFlagManagerError("Invalid flag '" + args[i]
                        + "' detected. Each flag must be seperated from its value by a space.");
            }
        }

        if (fileToLoad.isBlank())
        {
            logFlagManagerError("File to load is null");
        }

        if (printFlagStates)
        {
            printFlagStates();
        }
    }

    /*
     * Print out the states of all flags within this manager, used for
     * debugging.
     */
    private void printFlagStates()
    {
        System.out.println("fileToLoad " + fileToLoad);
        System.out.println("fileTypeBeingRead " + fileTypeBeingRead.toString());
        System.out.println("whatToDo " + whatToDo.toString());
        System.out.println("compileTimeChecking " + compileTimeChecking);
        System.out.println("optimiseCode " + optimiseCode);
        System.out.println("showWarnings " + showWarnings);
        System.out.println("stripUnusedCode " + stripUnusedCode);
        System.out.println("printFlagStates " + printFlagStates);
        System.out.println("enableDebugging " + enableDebugging);
        System.out.println("showResults " + showResults);
    }

    /*
     * Check if the provided string (toCheck) is an instance of flag. This
     * method adds the flag prefix to the flag.
     */
    private static boolean isFlag(String toCheck, String flag)
    {
        return toCheck.startsWith(FlagConstants.smallFlagPrefix + flag);
    }

    /*
     * Returns the first char of the argument at the index specified by 'next',
     * if it exists.
     */
    private static char valueOfNextChar(int next, String[] args)
    {
        char result = ' ';

        String nextString = valueOfNextString(next, args).toLowerCase();

        if (!nextString.isEmpty())
        {
            result = nextString.charAt(0);
        }

        return result;
    }

    /*
     * Gets the value of the argument at 'next', if it exists.
     */
    private static String valueOfNextString(int next, String[] args)
    {
        String nextString = "";

        if (isValidIndex(next, args))
        {
            nextString = args[next];
        }

        return nextString;
    }

    /*
     * Gets whether the argument at index 'next' specifies a true or false
     * value. Defaults to false.
     */
    private static boolean valueOfNext(int next, String[] args, boolean current)
    {
        boolean result = false;

        //  If the next value is valid, convert it to a boolean
        if (isValidIndex(next, args))
        {
            result = stringToBoolean(args[next]);
        }
        //  Otherwise just toggle the provided value
        else
        {
            result = !current;
        }

        return result;
    }

    /*
     * Check that the value of the argument at 'next' is valid to use. This
     * means that it exists and is not a flag.
     */
    private static boolean isValidIndex(int next, String[] args)
    {
        //  The next argument is only valid if it exists
        boolean nextIsValid = args.length > next;

        if (nextIsValid)
        {
            //  Check that the next argument is not a flag, and is not a file path (not perfect, but a good quick check)
            nextIsValid = !args[next].startsWith(FlagConstants.smallFlagPrefix);
        }

        return nextIsValid;
    }

    /*
     * Converts a string value to a boolean, using the array of 'true' strings in the FlagConstants.
     */
    private static boolean stringToBoolean(String string)
    {
        string = string.toLowerCase();

        return Support.stringInArray(string, FlagConstants.trueStrings);
    }

    /*
     * Checks if the specified argument is a flag.
     */
    private static boolean isFlagToken(int index, String[] args)
    {
        boolean result = index >= 0 && index < args.length;

        if (result)
        {
            result = args[index].startsWith(FlagConstants.smallFlagPrefix);
        }

        return result;
    }

    /*
     * Log a basic error message
     */
    private static void logFlagManagerError(String message)
    {
        System.out.println(message);
    }
}
