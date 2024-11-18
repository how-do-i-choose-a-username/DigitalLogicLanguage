package org.example.JavaJNI;

import java.util.jar.*;
import java.util.zip.ZipEntry;
import java.io.*;
import java.net.URISyntaxException;

import org.example.Language.Constants;

/*
 * Java class which manages the terminal using native C code.
 * Also responsible for calling those native methods.
 */
public class Terminal
{
    private static boolean isInitalised = false;
    private static boolean isWindows = false;

    private static final String libraryName = "terminal";
    private static final String macOSLibraryName = "lib" + libraryName + ".dylib";
    private static final String windowsLibraryName = libraryName + ".dll";

    /*
     * Initalise the terminal ready to receive input.
     */
    public static void initaliseTerminal()
    {
        disableBuffer();
        isInitalised = true;
    }

    /*
     * Sets the static flag to determine if the operating system is windows or
     * macos.
     */
    private static void checkIfWindows()
    {
        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            isWindows = true;
        }
    }

    /*
     * Restore the terminal to normal operation.
     */
    public static void packupTerminal()
    {
        enableBuffer();
        isInitalised = false;
    }

    /*
     * Get a single character from the terminal.
     */
    public static char getCharFromTerminal()
    {
        if (!isInitalised)
        {
            initaliseTerminal();
        }

        char character = getNextChar();

        //  The following code is to make windows behave like macos
        if (isWindows)
        {
            //  When enter is pressed we get a carraige return, it should be a newline
            if (character == '\r')
            {
                character = '\n';
            }
            //  Backspace shouldnt do anything
            else if (character == 8)
            {
                //  This is a non printable character, so it does nothing when trying to print it
                character = 0;
            }
            //  End of text, ie. Ctrl + C which on windows prints out a heart, but should immediatly terminate the program
            else if (character == 3)
            {
                System.exit(0);
            }
        }

        return character;
    }

    //  Call this at the end of the program
    private static native void enableBuffer();

    //  Call this at the start of the program
    private static native void disableBuffer();

    private static native char getNextChar();

    /*
     * Basically a static constructor. Checks if the program is running on
     * windows or mac, and then loads the native library.
     */
    static
    {
        checkIfWindows();

        try
        {
            loadTerminalLibrary();
        } catch (UnsatisfiedLinkError e)
        {
            //  If the previous load failed, we dont have the terminal library available, so extract it from the .jar and try again
            extractNativeLibrary();
            loadTerminalLibrary();
        }
    }

    /*
     * Wrapper method to load a native library. Used to load a library in the
     * same directory as where this is being run.
     */
    private static void loadTerminalLibrary() throws UnsatisfiedLinkError
    {
        //  Load the library of native C code to manage the terminal
        System.loadLibrary(libraryName);
    }

    /*
     * This method is to extract the correct native library from the jar file,
     * and save it in the same directory. Its loosely based off of the
     * stackoverflow post shown.
     */
    //  Thanks https://stackoverflow.com/questions/1529611/how-to-write-a-java-program-which-can-extract-a-jar-file-and-store-its-data-in-s
    private static void extractNativeLibrary()
    {
        //  Get the name of the jar file being run, which I know contains the native library files
        String jarFilePath = pathToJarFile();
        //  Get the name of the native library path
        String pathToNativeLibrary = jarFilePath.substring(0, jarFilePath.lastIndexOf(Constants.getFileSeperator())) + Constants.getFileSeperator();
        if (isWindows)
        {
            pathToNativeLibrary = pathToNativeLibrary + windowsLibraryName;
        }
        else
        {
            pathToNativeLibrary = pathToNativeLibrary + macOSLibraryName;
        }

        if (jarFilePath.isBlank())
        {
            throw new Error(
                    "Failed to find the path to the jar file that is currently being run. This may occur in the development enviroment, or because of bad file paths.");
        }

        //  Get a reference to the native library file from the jar file
        JarFile jarFile = null;
        ZipEntry fileEntry = null;
        try
        {
            jarFile = new JarFile(jarFilePath);

            if (jarFile != null)
            {
                if (isWindows)
                {
                    fileEntry = jarFile.getEntry(windowsLibraryName);
                }
                else
                {
                    fileEntry = jarFile.getEntry(macOSLibraryName);
                }
            }
        } catch (IOException e)
        {
            throw new Error("Failed to load the jar file.\n" + e.toString());
        }

        //  If we successfully found the library to load, now write it to a file
        if (fileEntry != null)
        {
            try
            {
                //  Copy the native library from within the jar to a file in the same directory as the jar
                File file = new File(pathToNativeLibrary);
                InputStream inputStream = jarFile.getInputStream(fileEntry);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(inputStream.readAllBytes());
                fileOutputStream.close();
                inputStream.close();
            } catch (IOException e)
            {
            }
        }

        //  Cleanup
        if (jarFile != null)
        {
            try
            {
                jarFile.close();
            } catch (IOException e)
            {
            }
        }
    }

    /*
     * Returns the path to the jar file that contains this class, which is the
     * jar file being run. If it fails, it returns an empty string.
     */
    private static String pathToJarFile()
    {
        String result = "";

        try
        {
            result = new File(Terminal.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        } catch (URISyntaxException e)
        {
            throw new Error(e.toString());
        }

        //  If it dosnt end with .jar, we dont want to try and load it
        if (!result.endsWith(".jar"))
        {
            result = "";
        }

        return result;
    }
}
