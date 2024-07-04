package com.morph.push;

import com.morph.general.FindWindowService;
import com.morph.general.PropertiesService;
import com.morph.jna.utils.User32;
import com.morph.jna.utils.WindowInfo;
import com.morph.natives.WindowsProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.morph.jna.utils.User32.SWP_NOZORDER;

public class SendPushService {
    private final static Logger logger = LoggerFactory.getLogger(SendPushService.class);

    private List<String> windowNames;

    private static SendPushService instance;
    private final Client pushClient;

    public static SendPushService instance() {
        if (instance == null) {
            instance = new SendPushService();
        }
        return instance;
    }

    public void initializeWindowNames(List<String> windowNames) {
        this.windowNames = windowNames;
    }

    private SendPushService() {
        String pushService = PropertiesService.instance().getString("push.service");
        logger.info("Push service choosen = {}", pushService);

        if (pushService == null) {
            throw new RuntimeException("Push service not set");
        }

        switch (pushService.toLowerCase()) {
            case "pushbullet":
                pushClient = new PushBulletService();
                pushClient.setPushListener(new PushListenerHandler());
                break;
            case "ntfy":
                pushClient = new NtfyService();
                pushClient.setPushListener(new PushListenerHandler());
                break;
            default:
                throw new RuntimeException("Push service not supported");
        }


    }

    public void reconnectIfPossible() {
        pushClient.reconnectIfPossible();
    }

    public void sendPush(String content, WindowInfo w) {
        pushClient.sendPush(content, w);
    }

    public void sendPushWithImage(File file, WindowInfo w) {
        pushClient.sendPushWithImage(file, w);
    }

    public void sendPushWithImage(File file, WindowInfo w, String message) {
        pushClient.sendPushWithImage(file, w, message);
    }

    class PushListenerHandler implements PushListener {

        @Override
        public void handle(List<String> messages) {
            if (messages.contains("$menu")) {
                pushClient.sendPush("""
                                GrowCastle-Watcher Menu:
                                \nSend "$1" to receive status image of GrowCastle
                                \nSend "$2" to unlock your pc after remote session ended""",
                        null);
            }
            if (messages.contains("$1")) {
                for (var window : FindWindowService.instance().fetchRelevantWindows(windowNames)) {
                    sendPushWithImage(window);
                }
            }
            if (messages.contains("$2")) {
                logger.info("Unlock rdp called");
                WindowsProcessService.instance().unlockAfterRdpSession();
                try {
                    Thread.sleep(10 * 1000);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                PointerInfo a = MouseInfo.getPointerInfo();
                Point b = a.getLocation();
                int mouse_x = (int) b.getX();
                int mouse_y = (int) b.getY();
                for (var window : FindWindowService.instance().fetchRelevantWindows(windowNames)) {
                    var x = window.getRect().left() + 10;
                    var y = window.getRect().top() + 10;

                    WindowInfo i = FindWindowService.instance().getFirstWindowInfo(window.getTitle());
                    User32.instance.BringWindowToTop(window.getHwnd());
                    User32.instance.SetForegroundWindow(window.getHwnd());

                    try {
                        Robot r = new Robot();
                        r.mouseMove(x, y);
                        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        User32.instance.SetWindowPos(window.getHwnd(),
                                0,
                                i.getRect().left(),
                                i.getRect().top(),
                                i.getRect().right() - i.getRect().left(),
                                i.getRect().bottom() - i.getRect().top(),
                                SWP_NOZORDER);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    logger.info("Bring window to the top executed");


                    User32.instance.MoveWindow(window.getHwnd(), i.getRect().left(), i.getRect().top(), i.getRect().right() - i.getRect().left(), i.getRect().bottom() - i.getRect().top(), true);
                }
            }
        }
    }

    private void sendPushWithImage(WindowInfo w) {
        try {
            Files.copy(Path.of(w.getTitle() + "_screen.png"), Path.of(w.getTitle() + "_temp.png"));
            sendPushWithImage(new File(w.getTitle() + "_temp.png"), w);
            Files.delete(Path.of(w.getTitle() + "_temp.png"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


}
