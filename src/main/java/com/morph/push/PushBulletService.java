package com.morph.push;

import com.github.sheigutn.pushbullet.Pushbullet;
import com.github.sheigutn.pushbullet.items.file.UploadFile;
import com.github.sheigutn.pushbullet.items.push.PushType;
import com.github.sheigutn.pushbullet.items.push.sendable.defaults.SendableFilePush;
import com.github.sheigutn.pushbullet.items.push.sendable.defaults.SendableNotePush;
import com.github.sheigutn.pushbullet.items.push.sent.Push;
import com.github.sheigutn.pushbullet.stream.PushbulletWebsocketClient;
import com.github.sheigutn.pushbullet.stream.PushbulletWebsocketListener;
import com.github.sheigutn.pushbullet.stream.message.StreamMessage;
import com.morph.general.PropertiesService;
import com.morph.jna.utils.WindowInfo;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class PushBulletService implements Client {
    private final static Logger logger = LoggerFactory.getLogger(PushBulletService.class);
    private Pushbullet pushbullet;
    private PushbulletListener pushbulletListener;
    private static PushbulletWebsocketClient client;

    public PushBulletService() {
        String accessToken = PropertiesService.instance().getString("pushbullet.token");

        if (accessToken != null) {
            pushbullet = new Pushbullet(accessToken);
            client = new PushbulletWebsocketClient(pushbullet);

            client.connect();

            client.getPushbullet().getAllPushes();
        } else {
            logger.warn("Access token is empty, push will not be send, check application.properties file");
        }
    }

    @Override
    public void sendPush(String content, WindowInfo w) {
        if (pushbullet != null) {
            String title = w != null ? w.getTitle() : "GrowCastle";
            pushbullet.push(new SendableNotePush(title, content));
        }
    }

    @Override
    public void sendPushWithImage(File file, WindowInfo w) {
        sendPushWithImage(file, w, null);
    }

    @Override
    public void sendPushWithImage(File file, WindowInfo w, String message) {
        if (pushbullet != null) {
            try {
                UploadFile uploadFile = pushbullet.uploadFile(file);
                if (uploadFile == null) {
                    pushbullet.push(new SendableNotePush(w.getTitle(), "upload file == null"));
                } else {
                    if (StringUtils.isBlank(message)) {
                        pushbullet.push(new SendableFilePush(w.getTitle(), uploadFile));
                    } else {
                        pushbullet.push(new SendableFilePush(w.getTitle() + " - " + message, uploadFile));
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void reconnectIfPossible() {
        if (client == null) {
            return;
        }

        if (!client.isConnected()) {
            logger.info("Client was disconnected, trying to reconnect");
            client.connect();
        }
    }

    @Override
    public void setPushListener(PushListener listener) {
        pushbulletListener = new PushbulletListener(listener);
        client.registerListener(pushbulletListener);
    }

    static class PushbulletListener implements PushbulletWebsocketListener {

        private static boolean firstRun = true;
        private final PushListener pushListener;

        public PushbulletListener(PushListener pushListener) {
            this.pushListener = pushListener;
        }

        @Override
        public void handle(Pushbullet pushbullet, StreamMessage streamMessage) {
            List<Push> newPushes = pushbullet.getNewPushes();

            if (firstRun) {
                firstRun = false;
                return;
            }

            var messages = newPushes.stream().filter(x -> x.getType().equals(PushType.NOTE)).map(x -> x.getBody().replaceAll("\\s", "")).toList();

            this.pushListener.handle(messages);

        }

    }
}
