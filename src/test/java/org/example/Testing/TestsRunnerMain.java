package org.example.Testing;
import java.io.*;

//  Tests the entire project
class TestsRunnerMain
{
    private static int testCount = 0;
    private static int failures = 0;
    private static String errorFileMessage = "";

    public static int getFailures() {
        return failures;
    }

    public static int getTestCount() {
        return testCount;
    }

    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            testCount = 0;
            failures = 0;
            errorFileMessage = "";

            long startTime = System.nanoTime();
            testCount += LanguageTests.runBasicTests();

            testCount += CompilationTests.runCompilerTests();
            long endTime = System.nanoTime();

            //  Duration in milliseconds
            long duration = (endTime - startTime) / 1000000;

            System.out.println(testCount + " tests have finished running with " + failures + " failures in " + duration
                    + " milliseconds.");

            writeToFile();
        }
    }

    private static void writeToFile()
    {
        try
        {
            FileWriter writer = new FileWriter("ErrorLog.txt");
            writer.write(errorFileMessage);
            writer.close();
        } catch (IOException e)
        {
            System.out.println("Failed to write to file " + e.getMessage());
        }
    }

    //  Log a failure to console, and count that it occurred
    public synchronized static void logFailure(String message)
    {
        failures += 1;
        System.out.println(message);
    }

    public synchronized static void logErrorFile(String title, String message)
    {
        errorFileMessage += title + "\n" + message + "\n\n";
    }

    public synchronized static void logErrorFileException(String title, Exception e)
    {
        String toWrite = title + "\n" + stackTraceString(e) + "\n\n";

        errorFileMessage += toWrite;
    }

    private static String stackTraceString(Exception e)
    {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
