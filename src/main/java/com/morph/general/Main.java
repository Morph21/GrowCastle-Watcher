package com.morph.general;

import com.morph.jna.utils.User32;
import com.morph.natives.WindowsProcessService;
import org.codehaus.plexus.util.StringUtils;
import com.morph.push.SendPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);
    private final static List<String> windowNames ;
    private final static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    static {
        logger.info(LogoPrint.print());
        logger.info("GrowCastle-watcher Start");
        logger.info("Initializing window names from properties");
        String windowNamesProp = PropertiesService.instance().getString("window.names");
        if (StringUtils.isBlank(windowNamesProp)) {
            throw new RuntimeException("window.names property not set");
        }
        windowNames = Arrays.asList(windowNamesProp.split(","));
        logger.info("window names initialized, found {}:", windowNames.size());
        windowNames.forEach(logger::info);
    }


    public static void main(String[] args) throws Exception {
        try {
            WindowsProcessService.instance().getRdpSessionIds();
            logger.info("Initializing Push service");
            SendPushService.instance().initializeWindowNames(windowNames);
            logger.info("Push service initialized");

            executor.scheduleWithFixedDelay(() -> {
                try {
                    Instant starts = Instant.now();
                    MainService.instance().run(windowNames);
                    Instant ends = Instant.now();

                    logger.info("Run end, took {} seconds ", new SimpleDateFormat("s.SSS").format(Duration.between(starts, ends).getNano() / 1000000));
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }, 0, 10, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            Runtime.getRuntime().exit(666);
        }


    }
}
