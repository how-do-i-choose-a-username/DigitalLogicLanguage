package org.example.Support;

import java.util.Scanner;

/*
 * This class is to help with debugging both Java and Digital Logic code.
 * It does this by providing a central location for warnings and messages to be output.
 */
public class Debugger
{
    //  This isn't a great solution, but it works. It's only used to pause compilation
    static Scanner sc;
    private static boolean debugging = false;
    public static boolean showWarnings = true;

    public static boolean isDebugging()
    {
        return debugging;
    }

    /*
     * Set whether the compiler outputs debug info as it compiles.
     */
    public static void setDebuggingMode(boolean mode)
    {
        debugging = mode;
        if (mode)
        {
            sc = new Scanner(System.in);
        }
    }

    /*
     * Set whether warnings are shown.
     */
    public static void setWarningMode(boolean mode)
    {
        showWarnings = mode;
    }

    /*
     * Reset this Debugger to its default settings.
     */
    public static void defaultSettings()
    {
        debugging = false;
        showWarnings = true;
    }

    /*
     * Log a debug message to the standard console, if they are being shown.
     */
    public static void logMessage(String title, String content)
    {
        if (debugging)
        {
            System.out.println(title + "\n" + content + "\n");
        }
    }

    /*
     * Log a warning to the standard console, if warnings are being shown.
     */
    public static void logWarning(String title, String message)
    {
        if (showWarnings)
        {
            System.out.println("WARNING: " + title + "\n" + message + "\n");
        }
    }

    /*
     * Pause the compiler until the user presses enter. Works by waiting for the
     * next line on the standard input. I am yet to have any problems.
     */
    public static void pause()
    {
        sc.nextLine();
    }
}
