package com.morph.general;

import com.morph.jna.utils.WindowHandler;
import com.morph.jna.utils.WindowInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class FindWindowService {
    private final static Logger logger = LoggerFactory.getLogger(FindWindowService.class);
    static {
        instance = new FindWindowService();
    }
    private static FindWindowService instance;
    private HashMap<String, WindowInfo> windowsFirstPos = null;
    private FindWindowService() {}

    public static FindWindowService instance() {
        return instance;
    }

    public List<WindowInfo> fetchRelevantWindows(List<String> windowNames) {
        try {
            var allWindows = WindowHandler.instance().listAllWindows(false);

            if (allWindows == null || allWindows.isEmpty()) {
                logger.info("Window named" + windowNames.stream().reduce((o1, o2) -> o1 + ", " + o2) + "not found");
                return null;
            }

            var windowsList = allWindows.stream().filter(w -> windowNames.contains(w.getTitle())).toList();

            if (windowsFirstPos == null) {
                windowsFirstPos = new HashMap<>();
                windowsList.forEach(w -> windowsFirstPos.put(w.getTitle(), w));
            }
            return windowsList;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public WindowInfo getFirstWindowInfo(String windowName) {
        return windowsFirstPos.get(windowName);
    }
}
