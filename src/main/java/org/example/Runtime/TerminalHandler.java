package org.example.Runtime;

import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.IOException;

/*
 * Java class which wraps a terminal handler.
 * The goal is to read a single character at a time.
 */
public class TerminalHandler
{
    private static Terminal terminal = null;
    private static boolean isWindows = false;

    /*
     * Initialise the terminal ready to receive input.
     */
    public static void initialiseTerminal()
    {
        try {
            terminal = TerminalBuilder.terminal();
            terminal.enterRawMode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (System.getProperty("os.name").toLowerCase().contains("windows"))
        {
            isWindows = true;
        }
    }

    /*
     * Close used resources
     */
    public static void packupTerminal()
    {
        if (terminal != null)
        {
            try {
                terminal.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /*
     * Get a single character from the terminal.
     */
    public static char getCharFromTerminal()
    {
        try {
            char character = (char)terminal.reader().read();

            //  The following code is to make windows behave like macOS
            if (isWindows)
            {
                //  When enter is pressed we get a carriage return, it should be a newline
                if (character == '\r')
                {
                    character = '\n';
                }
                //  Backspace shouldn't do anything
                else if (character == 8)
                {
                    //  This is a non-printable character, so it does nothing when trying to print it
                    character = 0;
                }
                //  End of text, i.e. Ctrl + C which on windows prints out a heart, but should immediately terminate the program
                else if (character == 3 || character == 4)
                {
                    System.exit(0);
                }
            }

            return character;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
