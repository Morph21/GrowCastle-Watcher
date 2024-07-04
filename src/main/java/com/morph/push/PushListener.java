package com.morph.push;

import java.util.List;

public interface PushListener {
    void handle(List<String> messages);
}
