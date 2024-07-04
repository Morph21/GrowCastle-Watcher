package com.morph.push;

import com.morph.jna.utils.WindowInfo;

import java.io.File;

public interface Client {

    void sendPush(String content, WindowInfo w);

    void sendPushWithImage(File file, WindowInfo w);

    void sendPushWithImage(File file, WindowInfo w, String message);

    void reconnectIfPossible();

    void setPushListener(PushListener listener);
}
