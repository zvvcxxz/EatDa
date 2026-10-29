package com.sp.app.controller;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

// 메인 재료 비교 : 확실한 동의어와 용도 표현만 정리
final class HomeIngredientMatcher {
    private HomeIngredientMatcher() {}

    private static final Map<String, String> ALIASES = Map.of(
            "쇠고기", "소고기",
            "달걀", "계란",
            "스파게티면", "스파게티",
            "스파게티니", "스파게티니",
            "파스타면", "파스타",
            "올리브유", "올리브오일");

    private static final Set<String> PASTA_TYPES = Set.of(
            "스파게티", "스파게티니", "링귀네", "페투치네", "펜네", "푸실리", "파르팔레", "마카로니");

    static boolean matches(String owned, String required) {
        String have = normalize(owned);
        String need = normalize(required);
        if (have.isEmpty() || need.isEmpty()) return false;
        if (have.equals(need)) return true;
        // 파스타 면처럼 포괄 이름으로 등록했을 때만 세부 면 이름도 인정
        // 스파게티를 보유했다고 펜네까지 보유한 것으로 처리하지 않음
        return (have.equals("파스타") && PASTA_TYPES.contains(need))
                || (need.equals("파스타") && PASTA_TYPES.contains(have));
    }

    // 문장 검색 : 쉼표와 조사로 구분한 재료를 비교 (무침 속 '무' 같은 부분 일치 제외)
    static boolean mentioned(String query, String required) {
        String[] words = query.toLowerCase(Locale.ROOT)
                .replaceAll("[,，·/!?！?]", " ").trim().split("\\s+");
        for (int start = 0; start < words.length; start++) {
            StringBuilder phrase = new StringBuilder();
            for (int end = start; end < Math.min(start + 3, words.length); end++) {
                String word = words[end].replaceFirst("(?:으로|이랑|하고|이나|와|과|랑|을|를|은|는|이|가|로)$", "");
                String literal = phrase.toString() + words[end];
                phrase.append(word);
                if (!matches(literal, required) && !matches(phrase.toString(), required)) continue;
                // 입력에서 명시적으로 제외한 재료는 일치로 세지 않음
                String following = String.join("", java.util.Arrays.copyOfRange(words, end + 1,
                        Math.min(end + 4, words.length)));
                if (following.matches("^(?:빼|제외|없이|말고|안넣|넣지않).*")) continue;
                return true;
            }
        }
        return false;
    }

    private static String normalize(String value) {
        if (value == null) return "";
        String name = value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
        name = name.replace("쇠고기", "소고기");
        // 실제 부위와 가공 상태는 남김 : 안심, 목살, 다진 고기 등
        if (name.startsWith("소고기") || name.startsWith("돼지고기")) {
            name = name.replaceFirst("(?:국거리(?:용)?|불고기용|찌개용|구이용|볶음용)$", "");
        }
        return ALIASES.getOrDefault(name, name);
    }
}
