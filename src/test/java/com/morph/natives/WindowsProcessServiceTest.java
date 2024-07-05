package com.morph.natives;

import org.codehaus.plexus.util.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WindowsProcessServiceTest {
    static List<String> result;

    static {
        result = new ArrayList<>();
        result.add(" USERNAME              SESSIONNAME        ID  STATE   IDLE TIME  LOGON TIME");
        result.add(">test                             4  Disc            .  04.07.2024 23:24");
    }

    @Test
    public void testSplit() {

        List<Integer> parsedList = new ArrayList<>();
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
        assertTrue(parsedList.contains(4));
    }
}
