package com.onlystudents.user.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SensitiveWordFilterService {

    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
            "admin", "root", "system", "test", "测试",
            "fuck", "shit", "damn", "bitch", "垃圾",
            "广告", "诈骗", "钓鱼", "色情", "赌博",
            "毛泽东", "邓小平", "习近平", "胡锦涛", "江泽民",
            "政府", "官员", "领导", "贪污", "腐败",
            "上访", "维权", "法轮功", "全能神", "邪教",
            "枪支", "毒品", "武器", "炸药", "核弹",
            "恐怖", "袭击", "爆炸", "暗杀", "颠覆",
            "分裂", "独立", "台独", "港独", "疆独",
            "卖国", "汉奸", "叛徒", "间谍", "特务"
    ));

    private static final Map<Character, Object> DFA_MAP = new HashMap<>();
    private static final Character END_FLAG = '\0';

    static {
        for (String word : SENSITIVE_WORDS) {
            Map<Character, Object> currentMap = DFA_MAP;
            for (char c : word.toCharArray()) {
                currentMap.computeIfAbsent(c, k -> new HashMap<>());
                currentMap = (Map<Character, Object>) currentMap.get(c);
            }
            currentMap.put(END_FLAG, Boolean.TRUE);
        }
    }

    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        Map<Character, Object> currentMap;
        StringBuilder tempWord = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            currentMap = DFA_MAP;

            if (!currentMap.containsKey(c)) {
                if (tempWord.length() > 0) {
                    result.append(tempWord.charAt(0));
                    tempWord.deleteCharAt(0);
                }
                result.append(c);
                continue;
            }

            tempWord.append(c);
            Object next = currentMap.get(c);

            while (next != null && next instanceof Map) {
                currentMap = (Map<Character, Object>) next;
                if (i + 1 < length) {
                    c = text.charAt(i + 1);
                    if (!currentMap.containsKey(c)) {
                        break;
                    }
                    tempWord.append(c);
                    next = currentMap.get(c);
                } else {
                    break;
                }
            }

            if (next != null && Boolean.TRUE.equals(next)) {
                int len = tempWord.length();
                for (int j = 0; j < len; j++) {
                    result.append("*");
                }
                tempWord.setLength(0);
            } else if (tempWord.length() > 0) {
                result.append(tempWord.charAt(0));
                tempWord.deleteCharAt(0);
            }
        }

        result.append(tempWord);
        return result.toString();
    }

    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            int matchLength = matchSensitiveWord(text, i);
            if (matchLength > 0) {
                return true;
            }
        }
        return false;
    }

    private int matchSensitiveWord(String text, int beginIndex) {
        Map<Character, Object> currentMap = DFA_MAP;
        int matchLength = 0;
        int tempLength = 0;

        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            currentMap = (Map<Character, Object>) currentMap.get(c);

            if (currentMap == null) {
                break;
            }

            tempLength++;
            if (currentMap.get(END_FLAG) != null && Boolean.TRUE.equals(currentMap.get(END_FLAG))) {
                matchLength = tempLength;
            }
        }

        return matchLength;
    }
}
