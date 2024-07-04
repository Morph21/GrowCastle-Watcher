package com.morph.jna.utils;

import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class WindowHandler {
    private final static Logger logger = LoggerFactory.getLogger(WindowHandler.class);

    static {
        instance = new WindowHandler();
    }

    private static final WindowHandler instance;

    private WindowHandler() {
    }

    public static WindowHandler instance() {
        return instance;
    }

    public List<WindowInfo> listAllWindows(boolean print) {
        final List<WindowInfo> inflList = new ArrayList<>();
        final List<Integer> order = new ArrayList<>();
        int top = User32.instance.GetTopWindow(0);
        while (top != 0) {
            order.add(top);
            top = User32.instance.GetWindow(top, User32.GW_HWNDNEXT);
        }

        User32.instance.EnumWindows((hWnd, lParam) -> {
            WindowInfo info = getWindowInfo(hWnd);
            inflList.add(info);
            return true;
        }, 0);

        inflList.sort(Comparator.comparingInt(o -> order.indexOf(o.hwnd)));
        if (print) {
            for (WindowInfo w : inflList) {
                logger.info(w.toString());
            }
        }

        return inflList;
    }

    public WindowInfo getWindowInfo(int hWnd) {
        Rect r = new Rect();
        User32.instance.GetWindowRect(hWnd, r);
        byte[] buffer = new byte[1024];
        User32.instance.GetWindowTextA(hWnd, buffer, buffer.length);
        String title = Native.toString(buffer);
        WindowInfo info = new WindowInfo(hWnd, r, title);
        return info;
    }

    public interface WndEnumProc extends StdCallLibrary.StdCallCallback {
        boolean callback(int hWnd, int lParam);
    }


}
