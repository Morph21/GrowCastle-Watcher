package com.morph.jna.utils;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;

public interface User32 extends StdCallLibrary {
    String SHELL_TRAY_WND = "Shell_TrayWnd";
    int WM_COMMAND = 0x111;
    int MIN_ALL = 0x1a3;
    int MIN_ALL_UNDO = 0x1a0;
    int SWP_NOSIZE = 0x0001;
    int SWP_NOMOVE = 0x0002;
    int SWP_NOZORDER = 0x0004;
    int SWP_SHOWWINDOW = 0x0040;
    int SWP_ASYNCWINDOWPOS = 0x4000;

    int MONITOR_DEFAULTTONULL = 0x00000000;
    int MONITOR_DEFAULTTOPRIMARY = 0x00000001;
    int MONITOR_DEFAULTTONEAREST = 0x00000002;


    User32 instance = Native.loadLibrary("user32", User32.class);

    boolean EnumWindows(WindowHandler.WndEnumProc wndenumproc, int lParam);

    boolean IsWindowVisible(int hWnd);

    int GetWindowRect(int hWnd, Rect r);

    void GetWindowTextA(int hWnd, byte[] buffer, int buflen);

    int GetTopWindow(int hWnd);

    int GetWindow(int hWnd, int flag);

    boolean ShowWindow(int hWnd);

    boolean BringWindowToTop(int hWnd);

    int GetActiveWindow();

    boolean SetForegroundWindow(int hWnd);

    int SetActiveWindow(int hWnd);

    int FindWindowA(String winClass, String title);

    long SendMessageA(int hWnd, int msg, int num1, int num2);

    int GW_HWNDNEXT = 2;

    boolean SetWindowPos(int hWnd, int hwndInsertAfter, int x, int y, int cx, int cy, int flags);

    boolean ShowWindow(int hWnd, int flag);

    int SetFocus(int hWnd);

    boolean MoveWindow(int hWnd, int X, int Y, int nWidth, int nHeight, boolean bRepaint);

    boolean SetProcessDPIAware();

    /**
     * Returns handle to HMonitor
     *
     * @param hwnd
     * @param dwFlags
     * @return
     */
    int MonitorFromWindow(int hwnd, int dwFlags);

}
