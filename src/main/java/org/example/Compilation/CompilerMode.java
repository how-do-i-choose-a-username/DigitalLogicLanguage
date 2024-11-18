package org.example.Compilation;

/* 
    What to do with the file being read.
    RunFile to just run it, Compile to save a compiled version,
    Automatic to update the compiled version (if necessary) then run it.
    Automatic compilation is unimplemented.
 */
public enum CompilerMode
{
    RunFile, Compile, Automatic
}