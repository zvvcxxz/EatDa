package com.sp.app.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MyFridgeService;
import com.sp.app.service.MyRecipeAiService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HomeRecipeMatchController {
    private final MyRecipeAiService recipes;
    private final MyFridgeService fridge;

    public record Search(String keyword) {}

    // 메인 검색 : 기존 추천 결과에 내 냉장고 재료 비교만 추가
    @PostMapping("/home/recipe-search")
    public Map<String, Object> search(@RequestBody Search search) {
        if (search.keyword() == null || search.keyword().isBlank() || search.keyword().length() > 500) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "검색어를 500자 이내로 입력해주세요.");
        }
        var info = LoginMemberUtil.getSessionInfo();
        Set<String> owned = new HashSet<>();
        boolean compare = info != null && info.getMember_id() != null;
        if (compare) {
            for (var item : fridge.getMemberFridgeItems(info.getMember_id())) {
                if (!item.isExpired()) {
                    String name = normalize(item.getIngredientName());
                    owned.add(name);
                    if (name.equals("계란") || name.equals("달걀")) { owned.add("계란"); owned.add("달걀"); }
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> source : recipes.recommendRecipes(search.keyword().trim())) {
            Map<String, Object> row = new HashMap<>(source);
            Object value = row.get("rcp_parts_dtls");
            if (value == null) value = row.get("rcpPartsDtls");
            List<String> required = parse(value == null ? "" : value.toString());
            if (compare && !required.isEmpty()) {
                row.put("haveIngredients", required.stream().filter(x -> owned.contains(normalize(x))).toList());
                row.put("missingIngredients", required.stream().filter(x -> !owned.contains(normalize(x))).toList());
            }
            result.add(row);
        }
        return Map.of("recipes", result, "comparisonEnabled", compare);
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    // 재료 설명 : 괄호 수량과 구분 제목을 빼고 이름만 비교
    List<String> parse(String value) {
        String text = value.replaceAll("\\[[^\\]]*\\]", "\n").replaceAll("\\([^)]*\\)|（[^）]*）", "");
        Set<String> names = new LinkedHashSet<>();
        for (String item : text.split("[,\\r\\n]+")) {
            String name = item.replaceFirst("\\s*\\d+(?:\\.\\d+)?(?:/\\d+)?\\s*(?:kg|mg|ml|g|L|l|개|모|컵|큰술|작은술|줄기|줄|쪽|장|통|cm|mm|스푼|줌|꼬집|알|마리|팩|봉|캔|토막|근|포기|뿌리|대|T|t).*$", "")
                    .replaceFirst("(약간|적당량|적당히|조금|취향껏)$", "").trim();
            if (!name.isBlank()) names.add(name);
        }
        return List.copyOf(names);
    }
}
