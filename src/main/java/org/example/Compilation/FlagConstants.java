package org.example.Compilation;

/*
 * Class to store the constants used to specify flags for the flag manager
 */
class FlagConstants
{
    public static final String smallFlagPrefix = "-";

    public static final String filePathFlag = "f";
    public static final String fileTypeFlag = "t";
    public static final String compilerModeFlag = "m";
    public static final String compilerChecksFlag = "c";
    public static final String optimiseCodeFlag = "Z";
    public static final String silenceWarningsFlag = "w";
    public static final String stripCodeFlag = "s";
    public static final String printFlagState = "p";
    public static final String debuggingEnabled = "d";
    public static final String showResults = "r";
    public static final String showTimings = "T";

    public static final String[] trueStrings = new String[]
    { "yes", "true", "1", "on", "y" };
}
