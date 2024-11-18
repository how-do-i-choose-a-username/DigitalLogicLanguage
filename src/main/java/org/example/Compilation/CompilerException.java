package org.example.Compilation;

/*
 * Basic exception for the compilation process, only available in the compilation package.
 */
class CompilerException extends Exception
{
	public CompilerException(String errorMessage)
	{
		super(errorMessage);
	}
}