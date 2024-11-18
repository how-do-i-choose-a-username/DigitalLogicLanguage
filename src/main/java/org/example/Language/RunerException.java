package org.example.Language;

/*
 * Basic exception for when running digital logic code.
 */
class RunerException extends Exception
{
	public RunerException(String errorMessage)
	{
		super(errorMessage);
	}
}