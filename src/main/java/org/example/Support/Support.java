package org.example.Support;

import java.util.*;

import java.io.*;

/*
 * Class of miscellanous support functions used through out the script.
 */
public class Support
{
    private static final char onBitChar = '1';
    private static final char offBitChar = '0';
    private static final int maxBitStringSize = 32;

    /*
     * Check that the provided token is a language constant, so made up of '1'
     * and '0'.
     */
    public static boolean isConstantToken(String token)
    {
        boolean validString = true;
        for (int i = 0; i < token.length(); i++)
        {
            validString = validString && (token.charAt(i) == onBitChar || token.charAt(i) == offBitChar);
        }
        return validString;
    }

    public static boolean isNumeric(String token)
    {
        boolean isNumeric = false;
        try
        {
            Integer.valueOf(token);
            isNumeric = true;
        } catch (NumberFormatException e)
        {
        }
        return isNumeric;
    }

    /*
     * Checks if the provided string is in the provided array.
     */
    public static boolean stringInArray(String string, String[] array)
    {
        return Arrays.asList(array).contains(string);
    }

    /*
     * Convert a constant value specified in '1' and '0' to an integer value.
     */
    public static int bitsToInt(String bits)
    {
        if (bits.length() > maxBitStringSize)
        {
            //bits = bits.substring(bits.length() - maxStringSize, bits.length());
            Debugger.logWarning("Oversize constant",
                    "The constant '" + bits + "' is too long. It has a length of " + bits.length()
                            + " when the max length is " + maxBitStringSize + ". The constant will be trimmed to "
                            + maxBitStringSize + " ignoring the leftmost bits.");
        }

        //  I used to use 'Integer.parseInt(bits, 2);' but it dosnt work when you try to specify the most significant bit. 

        int result = 0;
        for (int i = 0; i < maxBitStringSize && i < bits.length(); i++)
        {
            //  If the char is on, set the corresponding bit in the int to true
            //  We need to loop backwards through the string
            if (bits.charAt(bits.length() - 1 - i) == onBitChar)
            {
                result = result | (1 << i);
            }
            //  If its not on, we dont need to do anything else, it defaults to off
        }

        return result;
    }

    /*
     * Returns a string that is the provided string, repeated the provided
     * amount of times.
     */
    public static String multiplyString(int amount, String string)
    {
        String result = "";

        for (int i = 0; i < amount; i++)
        {
            result += string;
        }

        return result;
    }

    /*
     * Writes a serialized object to the specified file location. Taken from DSA
     * week 4.
     */
    public static void save(Serializable objectToSave, String fileName)
    {
        FileOutputStream fileStream;
        ObjectOutputStream objectStream;

        try
        {
            fileStream = new FileOutputStream(fileName);
            objectStream = new ObjectOutputStream(fileStream);
            objectStream.writeObject(objectToSave);

            objectStream.close();
        } catch (Exception e)
        {
            System.out.println("Failed to save object " + e.getMessage());
        }
    }

    /*
     * Loads a serialized object from the specified file location. Taken from
     * DSA week 4.
     */
    public static Serializable load(String fileName)
    {
        FileInputStream fileStream;
        ObjectInputStream objectStream;
        Serializable objectBeingRead = null;

        try
        {
            fileStream = new FileInputStream(fileName);
            objectStream = new ObjectInputStream(fileStream);
            objectBeingRead = (Serializable) objectStream.readObject();

            objectStream.close();
        } catch (Exception e)
        {
            System.out.println("Failed to load object " + e.getMessage());
        }

        return objectBeingRead;
    }

    /*
     * Converts an integer object to an int. If its null, it has a zero value.
     */
    public static int integerToInt(Integer integer)
    {
        int result = 0;
        if (integer != null)
        {
            result = integer;
        }
        return result;
    }
}
