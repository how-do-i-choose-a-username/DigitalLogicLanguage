package org.example.Testing;

import java.io.*;
import java.util.*;

import org.example.Runtime.LanguageConstants;
import org.example.Compilation.LanguageParser;
import org.example.Language.StatementProgram;
import org.example.Support.Support;

//  This script is messy, but it mostly works. I cant be bothered working on it more, testing the test script is hard
/*
 * This class is responsible for running all the automatic tests.
 * These are tests I have written in Digital Logic.
 * This class compiles them, and optionally runs them aswell, depending on the test.
 */
class CompilationTests
{
    private List<Thread> threads = new ArrayList<>();
    private static int testCount = 0;

    /*
     * Runs all of the test scripts, and executes them aswell if it specifies a
     * return value. Wraps the function that starts all of the actual tests.
     */
    public static int runCompilerTests()
    {
        CompilationTests runner = new CompilationTests();
        String directorPath = "CompilationTests/";
        runCompilerTestsInternal(directorPath, runner);

        try
        {
            //  Wait for all the threads to finish
            for (Thread thread : runner.threads)
            {
                thread.join();
            }
        } catch (InterruptedException e)
        {
            TestsRunnerMain.logFailure("Failed while waiting for all the threads to finish\n" + e.toString());
        }

        return testCount;
    }

    /*
     * Recursively open every folder, and create a thread for each test found.
     */
    private static void runCompilerTestsInternal(String path, CompilationTests runner)
    {
        File dir = new File(path);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null)
        {
            for (File child : directoryListing)
            {
                if (child.isDirectory())
                {
                    runCompilerTestsInternal(path + child.getName() + "/", runner);
                }
                else if (child.getName().endsWith(LanguageConstants.sourceFileExtension)
                        || child.getName().endsWith(LanguageConstants.failureTestSourceExtension))
                {
                    testCount += 1;

                    //  Create a new thread, then run it
                    Thread newThread = new Thread(runner.new SingleTestRun(child, path), child.getName());
                    newThread.start();
                    runner.threads.add(newThread);
                }
            }
        }
    }

    /*
     * This is a simple class which is used by the threading system to run a
     * test.
     */
    public class SingleTestRun implements Runnable
    {
        private File child;
        private String path;

        //  Constructor
        public SingleTestRun(File child, String path)
        {
            this.child = child;
            this.path = path;
        }

        /*
         * Actually run the test associated with this class.
         */
        @Override
        public void run()
        {
            if (child.getName().endsWith(LanguageConstants.sourceFileExtension))
            {
                normalTest();
            }
            //  Test to run when the script has the extension '.notdl' and I want it to fail
            else
            {
                failureTest();
            }
        }

        /*
         * Run a normal test that is compiled and run with the goal of success.
         */
        private void normalTest()
        {
            try
            {
                String targetValue = findTargetValue(child);

                String fileName = path + child.getName();

                StatementProgram loadedProgram = compileToProgram(fileName);

                checkReturnedValue(fileName, targetValue, loadedProgram);
            } catch (Exception e)
            {
                String message = "Catastrophic failure while running thread";
                TestsRunnerMain.logFailure(message);
                TestsRunnerMain.logErrorFileException(message, e);
            }
        }

        /*
         * Run a failure test which must fail to be counted as a success.
         */
        private void failureTest()
        {
            try
            {
                String fileName = path + child.getName();
                LanguageParser.parseFile(fileName).getValueObject();

                String message = "Invalid file compiled and run '" + fileName + "'";
                TestsRunnerMain.logFailure(message);
                TestsRunnerMain.logErrorFile(message, "The file '" + fileName
                        + "' was successfully compiled and run, but its a '.notdl' file, which indicates that it should fail to compile and run.");
            } catch (Exception e)
            {
                //  This is the desired result
            }
        }
    }

    /*
     * Convert the expected value string into an int. Used when a test specifies
     * a return value.
     */
    private static int expectedValueToInt(String expected)
    {
        int result;
        //  If its all true values, and the file being tested used the lazy syntax
        if (expected.equals("true"))
        {
            //  Bitwise not
            result = ~0;
        }
        //  If the file is using the lazy syntax and its false
        else if (expected.equals("false"))
        {
            result = 0;
        }
        //  Default way of checking
        else
        {
            result = Support.bitsToInt(expected);
        }
        return result;
    }

    /*
     * Find the target value in a test file, if it exists. If a test is only
     * supposed to be compiled, it wont specify a target value.
     */
    private static String findTargetValue(File child)
    {
        //  Find the target value in the script
        String targetValue = "";
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(child));
            targetValue = reader.readLine();
            if (targetValue.contains("Returns"))
            {
                targetValue = targetValue.substring(targetValue.indexOf('<') + 1, targetValue.indexOf('>'));
                targetValue = targetValue.toLowerCase();
            }
            else
            {
                targetValue = "";
            }
            reader.close();
        } catch (Exception e)
        {
            //  Special case for a test which is an empty file. Test breaks the test script
            if (targetValue == null)
            {
                targetValue = "";
            }
            else
            {
                String message = "Failed to open file to read first line";
                TestsRunnerMain.logFailure(message);
                TestsRunnerMain.logErrorFileException(message, e);
            }
        }

        return targetValue;
    }

    /*
     * Compiles the specified program and returns the runnable result.
     */
    private static StatementProgram compileToProgram(String fileName)
    {
        //  Load the script (Compile)
        StatementProgram loadedProgram = null;
        try
        {
            loadedProgram = LanguageParser.parseFile(fileName);
        } catch (Exception e)
        {
            String message = "Failed to compile the file '" + fileName + "'";
            TestsRunnerMain.logFailure(message);
            TestsRunnerMain.logErrorFileException(message, e);
            loadedProgram = null;
        }

        return loadedProgram;
    }

    /*
     * Run the test, and check that the value that was returned is the value
     * that was expected.
     */
    private static void checkReturnedValue(String fileName, String targetValue, StatementProgram loadedProgram)
            throws Exception
    {
        //  Execute the script if it loaded and specifies a return value
        if (!targetValue.isEmpty() && loadedProgram != null)
        {
            Integer result = null;
            boolean success = false;
            //  Actually try running the program, wrap it in a catch incase it fails
            try
            {
                result = loadedProgram.getValueObject();
                success = true;
            } catch (Exception e)
            {
                String message = "Failed to calculate a value from file '" + fileName + "'";
                TestsRunnerMain.logFailure(message);
                TestsRunnerMain.logErrorFileException(message, e);
            }
            int expected = expectedValueToInt(targetValue);

            //  Check that we got a value, fail if we didnt. We only run the program if a value is expected
            if (success)
            {
                if (result == null)
                {
                    String message = "Failed to return a value from the file '" + fileName
                            + "'. A value was expected, but never returned.";
                    TestsRunnerMain.logFailure(message);
                    TestsRunnerMain.logErrorFile(fileName, message);
                }
                //  Check that we calculated the correct value
                else if (expected != result)
                {
                    String message = "Failed to return correct value from file '" + fileName + "'";
                    String otherMessage = "The file '" + fileName + "' returned a value of "
                            + Integer.toBinaryString(result) + " when a value of " + Integer.toBinaryString(expected)
                            + " was expected. \nThe XOR difference between them is "
                            + Integer.toBinaryString(result ^ expected);
                    TestsRunnerMain.logFailure(message);
                    TestsRunnerMain.logErrorFile(message, otherMessage);
                }
            }
        }
    }
}
