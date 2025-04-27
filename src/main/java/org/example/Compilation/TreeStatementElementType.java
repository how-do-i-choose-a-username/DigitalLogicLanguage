package org.example.Compilation;

/*
    Used to store the type of each element. 
    Unknown is for when an element has a token, but what exactly that token is currently unknown
    Variable is for when the token contains a variable name
    Constant is for when a token contains 0s and 1s, because it is a constant value in the code
    Function is when an element refers to a function name to be called. It may or may not be setup yet, its just what it is
    Solo builtin is for a builtin that only takes a single parameter
    Dual builtin is for a builtin that takes two parameters
    Intermediate is for processes in the compiler, such as block operators, etc.
*/
enum TreeStatementElementType {Unknown, Variable, Constant, Function, SoloBuiltin, DualBuiltin, Intermediate, NullType }