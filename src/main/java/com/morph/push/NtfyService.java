package com.morph.push;

import com.google.gson.Gson;
import com.morph.general.PropertiesService;
import com.morph.jna.utils.WindowInfo;
import com.morph.models.NtfyMessage;
import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NtfyService implements Client {
    private final static Logger logger = LoggerFactory.getLogger(NtfyService.class);

    private final static String topic = PropertiesService.instance().getString("ntfy.topic", null);
    private final static String token = PropertiesService.instance().getString("ntfy.token", null);
    private final static ExecutorService executor = Executors.newFixedThreadPool(1);
    private boolean isConnected = false;
    private PushListener listener;
    private final Gson gson = new Gson();

    public NtfyService() {
        if (StringUtils.isBlank(topic)) {
            throw new RuntimeException("ntfy.topic not set");
        }

        if (StringUtils.isBlank(token)) {
            throw new RuntimeException("ntfy.token not set");
        }
    }

    @Override
    public void sendPush(String content, WindowInfo w) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh/" + topic))
                    .POST(HttpRequest.BodyPublishers.ofString(content))
                    .header("title", w != null ? w.getTitle() : "GrowCastle")
                    .header("Authorization", "Bearer " + token)
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void sendPushWithImage(File file, WindowInfo w) {
        sendPushWithImage(file, w, null);
    }

    @Override
    public void sendPushWithImage(File file, WindowInfo w, String message) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create("https://ntfy.sh/" + topic))
                    .PUT(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                    .header("title", w != null ? w.getTitle() : "GrowCastle")
                    .header("Filename", file.getName())
                    .header("Authorization", "Bearer " + token);

            if (StringUtils.isNotBlank(message)) {
                builder.header("message", message);
            }
            HttpRequest request = builder.build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void reconnectIfPossible() {
        if (!isConnected) {
            logger.info("Client was disconnected, trying to reconnect");
            connectToListener(this.listener);
        }
    }

    @Override
    public void setPushListener(PushListener listener) {
        this.listener = listener;
        connectToListener(listener);
    }

    private void connectToListener(PushListener listener) {
        executor.execute(() -> {
            BufferedReader in = null;
            try {
                URL url = new URL("https://ntfy.sh/" + topic + "/json");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                isConnected = true;
                connection.setRequestProperty("Authorization", "Bearer " + token);
                connection.setRequestMethod("GET");
                connection.setDoOutput(true);
                connection.setDoInput(true);


                in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {

                    NtfyMessage ntfyMessage = gson.fromJson(inputLine, NtfyMessage.class);
                    if (ntfyMessage != null && ntfyMessage.getEvent().equals("message")) {
                        listener.handle(Collections.singletonList(ntfyMessage.getMessage()));
                    }
                }
                in.close();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                isConnected = false;
            }
        });
    }
}
