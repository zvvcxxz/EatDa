package com.sp.app.controller;

import java.util.*;
import org.springframework.web.bind.annotation.*;
import com.sp.app.service.MyRecipeAiService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class HomeRecipeMatchController {
    private final MyRecipeAiService recipes;

    public record Search(String keyword) {}

    public record IngredientSearch(List<String> ingredients) {}

    // 선택 재료 검색 : 조원 서비스는 조회만 사용하고 메인에서 일치 재료를 다시 비교
    @PostMapping("/home/recipe-ingredients")
    public Map<String, Object> ingredients(@RequestBody IngredientSearch request) {
        if (request.ingredients() == null || request.ingredients().isEmpty()
                || request.ingredients().size() > 30
                || request.ingredients().stream().anyMatch(x -> x == null || x.isBlank() || x.length() > 100)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "재료를 1~30개 선택해주세요.");
        }
        List<String> selected = request.ingredients().stream().map(String::trim).distinct().toList();
        return compareRecipes(recipes.recommendRecipes(selected), selected, true);
    }

    // AI 검색 : 입력한 문장에 적힌 재료와 비교
    @PostMapping("/home/recipe-search")
    public Map<String, Object> search(@RequestBody Search search) {
        if (search.keyword() == null || search.keyword().isBlank() || search.keyword().length() > 500) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "검색어를 500자 이내로 입력해주세요.");
        }
        String keyword = search.keyword().trim();
        return compareRecipes(recipes.recommendRecipes(keyword), List.of(), true, keyword);
    }

    // 비교 결과 : 선택 재료 검색과 문장 검색에서 같은 기준 사용
    private Map<String, Object> compareRecipes(List<Map<String, Object>> sources,
            List<String> owned, boolean compare) {
        return compareRecipes(sources, owned, compare, null);
    }

    private Map<String, Object> compareRecipes(List<Map<String, Object>> sources,
            List<String> owned, boolean compare, String keyword) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> source : sources) {
            Map<String, Object> row = new HashMap<>(source);
            Object value = row.get("rcp_parts_dtls");
            if (value == null) value = row.get("rcpPartsDtls");
            List<String> required = parse(value == null ? "" : value.toString());
            // 이전 서비스에서 계산한 값이 남지 않게 제거
            row.remove("haveIngredients");
            row.remove("missingIngredients");
            row.remove("matchRate");
            if (compare && !required.isEmpty()) {
                List<String> have = required.stream().filter(x -> keyword != null
                        ? HomeIngredientMatcher.mentioned(keyword, x)
                        : owned.stream().anyMatch(y -> HomeIngredientMatcher.matches(y, x))).toList();
                List<String> missing = required.stream().filter(x -> !have.contains(x)).toList();
                row.put("haveIngredients", have);
                row.put("missingIngredients", missing);
                row.put("matchRate", (int) Math.round(have.size() * 100.0 / required.size()));
            }
            result.add(row);
        }
        return Map.of("recipes", result, "comparisonEnabled", compare);
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
