This is a folder of compilation tests. As part of running the test script, it tries to compile every script in this folder. Some of them are also run. 
They should all be logically valid scripts to compile, their purpose is to stress test the compiler. I may have made syntax mistakes, but I shouldn't have.
The name of each script indicates what it is testing. As I add new compiler features I should add appropriate tests to this folder.
There are also scripts in this folder which are run after being compiled, sorted by the folder they are in, but also because they start with the line '# Returns<return-value>'
The return value can be a binary constant, made up of 0s and 1s, or 'true' or 'false' to indicate a bit strings of 1s or 0s respectively.
A lot of the functionality being tested has a separate compilation and runtime version, I should probably not do this
There is also the folder FailureChecks, which contains '.notdl' files, each of which must fail to compile or run to succesfully pass the test

Currently untested functionality:
Dual functions with blocks on both sides