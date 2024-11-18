#include<jni.h>
#include<stdio.h>
#ifdef __MACH__
#include<termios.h>
#endif
#ifdef __WIN32
#include<conio.h>
#endif
#include"JavaJNICode_Terminal.h"

/*
    The C code in this project is to enable/disable terminal echo and buffering. 
    It is losely based off of UCP assignment 1, from Uni
    Idk how to do this in Java, but I can do it in C, and call C from Java so thats whats happening

    This single C file is used for both Windows and macOS. __MACH__ is a definition to check if the platform is macOS.
    I can use __WIN32 to check if the platform is windows.
*/

/* Important note. To generate the header file for this script, I ran the command 'javac -h . Terminal.java' */

/* Get the next char from the terminal */
JNIEXPORT jchar JNICALL Java_JavaJNICode_Terminal_getNextChar(JNIEnv *a, jclass b)
{
#ifdef __MACH__
    char input;

    scanf("%c", &input);

    return input;
#endif
#ifdef __WIN32
    return _getch();
#endif
}

/* Disable the buffers, call this at the start of the program */
JNIEXPORT void JNICALL Java_JavaJNICode_Terminal_disableBuffer(JNIEnv *a, jobject b)
{
/* Only compile this code on macos */
#ifdef __MACH__
    struct termios mode;

    tcgetattr(0, &mode);
    mode.c_lflag &= ~(ECHO | ICANON);
    tcsetattr(0, TCSANOW, &mode);
#endif
}

/* Renable the buffers to restore normal operation */
JNIEXPORT void JNICALL Java_JavaJNICode_Terminal_enableBuffer(JNIEnv *a, jobject b)
{
#ifdef __MACH__
    struct termios mode;

    tcgetattr(0, &mode);
    mode.c_lflag |= (ECHO | ICANON);
    tcsetattr(0, TCSANOW, &mode);
#endif
}
