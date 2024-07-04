package com.morph.jna.utils;

public class WindowInfo {
    int hwnd;
    Rect rect;
    String title;

    public WindowInfo(int hwnd, Rect rect, String title) {
        this.hwnd = hwnd;
        this.rect = rect;
        this.title = title;
    }

    @Override
    public String toString() {
        return String.format("(%d,%d)-(%d,%d) : \"%s\"", rect.left, rect.top, rect.right, rect.bottom, title);
    }

    public int getHwnd() {
        return hwnd;
    }

    public Rect getRect() {
        return rect;
    }

    public String getTitle() {
        return title;
    }
}
