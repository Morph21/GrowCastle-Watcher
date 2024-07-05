package com.morph.natives;

import org.codehaus.plexus.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WindowsProcessService {
    private final static Logger logger = LoggerFactory.getLogger(WindowsProcessService.class);

    static {
        instance = new WindowsProcessService();
    }

    private static WindowsProcessService instance;

    private WindowsProcessService() {
    }

    public static WindowsProcessService instance() {
        return instance;
    }

    public void unlockAfterRdpSession() {
        List<Integer> rdpSessionIds = getRdpSessionIds();
        if (rdpSessionIds == null || rdpSessionIds.isEmpty()) {
            logger.info("Didn't find rdp session id active");
            return;
        }
        logger.info("Trying to log you in after rdp session closed, going to try for each session found");
        for (Integer sessionId : rdpSessionIds) {
            try {
                executeCommand("%windir%\\System32\\tscon.exe " + sessionId + " /dest:console");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public List<Integer> getRdpSessionIds() {
        List<String> result = executeCommand("query user");
        if (result == null || result.isEmpty()) {
            return null;
        }

        List<Integer> parsedList = null;
        try {

            parsedList = new ArrayList<>();

            for (int i = 1; i < result.size(); i++) {
                String s = result.get(i);

                List<String> split = Arrays.stream(s.split("\s")).filter(StringUtils::isNotBlank).toList();
                for (String str : split) {
                    boolean isDigit = str.chars().mapToObj(x -> (char)x).allMatch(Character::isDigit);
                    if (isDigit) {
                        parsedList.add(Integer.parseInt(str));
                    }
                }
            }
            return parsedList;
        } catch (Exception e){
            result.forEach(logger::error);
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public List<String> executeCommand(String command) {
        try {
            Runtime rt = Runtime.getRuntime();
            String[] commands = {"cmd.exe", "/c", command};
            Process proc = rt.exec(commands);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(proc.getErrorStream()));

            var result = stdInput.lines().toList();
            var error = stdError.lines().toList();

            List<String> resultCombinedWithError = new ArrayList<>();

            resultCombinedWithError.addAll(result);
            resultCombinedWithError.addAll(error);

            return resultCombinedWithError;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
