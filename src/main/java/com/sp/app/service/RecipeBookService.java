package com.sp.app.service;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.sp.app.domain.dto.MyRecipeAiDto;
import com.sp.app.domain.dto.RecipeBookItemDto;
import com.sp.app.postgres.mapper.RecipeBookMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipeBookService {
    private static final int PAGE_SIZE = 8;
    private final RecipeBookMapper mapper;

    public record BookPage(List<RecipeBookItemDto> items, long total, long page,
            int pageSize, long totalPages) {}

    // 목록 조회 : 같은 기준으로 전체 수와 현재 8개 조회
    @Transactional(transactionManager = "postgresTransactionManager", readOnly = true,
            isolation = Isolation.REPEATABLE_READ)
    public BookPage list(long page, String keyword, String ingredient, String category, String method) {
        if (page < 1) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "페이지 번호가 올바르지 않습니다.");
        String word = pattern(clean(keyword, 100));
        String material = pattern(clean(ingredient, 100));
        String type = clean(category, 60);
        String way = clean(method, 60);
        long total = mapper.countRecipes(word, material, type, way);
        long totalPages = Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE);
        long current = Math.min(page, totalPages);
        List<RecipeBookItemDto> items = mapper.listRecipes(word, material, type, way,
                (current - 1) * PAGE_SIZE, PAGE_SIZE);
        return new BookPage(items, total, current, PAGE_SIZE, totalPages);
    }

    // 필터 목록 : DB에 실제 등록된 종류와 조리 방법만 사용
    public Map<String, List<String>> filters() {
        return Map.of("categories", mapper.listCategories(), "methods", mapper.listMethods());
    }

    // 상세 조회 : AI 호출 없이 조리 정보 반환
    public MyRecipeAiDto detail(String id) {
        String key = clean(id, 40);
        if (key.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "레시피 번호가 없습니다.");
        MyRecipeAiDto recipe = mapper.findRecipe(key);
        if (recipe == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "레시피를 찾을 수 없습니다.");
        return recipe;
    }

    // 검색 조건 : 길이 확인과 앞뒤 공백 제거
    private String clean(String value, int limit) {
        String text = value == null ? "" : value.trim();
        if (text.length() > limit) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "검색어가 너무 깁니다.");
        return text;
    }

    // 재료 / 이름 검색 : SQL 와일드카드는 문자 그대로 비교
    private String pattern(String value) {
        if (value.isEmpty()) return "";
        return "%" + value.replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
    }
}
