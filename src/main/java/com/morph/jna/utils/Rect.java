package com.morph.jna.utils;

import com.morph.general.PropertiesService;
import com.sun.jna.Structure;

import java.util.ArrayList;
import java.util.List;

public class Rect extends Structure {
    public int left, top, right, bottom;

    private final double scale = PropertiesService.instance().getDouble("scale", 1.0);

    @Override
    protected List<String> getFieldOrder() {
        List<String> order = new ArrayList<>();
        order.add("left");
        order.add("top");
        order.add("right");
        order.add("bottom");
        return order;
    }

    public int left() {
        return (int)(left / scale);
    }

    public int right() {
        return (int)(right / scale);
    }

    public int top() {
        return (int)(top / scale);
    }

    public int bottom() {
        return (int)(bottom / scale);
    }
}
