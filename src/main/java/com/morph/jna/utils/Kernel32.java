package com.morph.jna.utils;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public interface Kernel32 extends StdCallLibrary {

    Kernel32 instance = Native.loadLibrary("Kernel32", Kernel32.class);

    int GetLastError();

}

