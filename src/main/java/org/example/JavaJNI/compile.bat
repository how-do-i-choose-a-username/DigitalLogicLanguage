::  This is to compile a native library on windows, currently untested
gcc -c -I"%JAVA_HOME%\include" -I"%JAVA_HOME%\include\win32" JavaJNICode_Terminal.c -o JavaJNICode_Terminal.o
gcc -shared -o ../terminal.dll JavaJNICode_Terminal.o -Wl,--add-stdcall-alias
pause