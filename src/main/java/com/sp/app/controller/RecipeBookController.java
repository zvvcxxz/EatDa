package com.sp.app.controller;

import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.RecipeBookService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/home/recipe-book")
public class RecipeBookController {
    private final RecipeBookService service;

    // 레시피북 : 전체 / 종류 / 조리 방법 / 재료 탐색 화면
    @GetMapping({"", "/"})
    public String view(@RequestParam(name = "mode", defaultValue = "all") String mode, Model model) {
        model.addAttribute("bookMode", List.of("all", "type", "method", "ingredient").contains(mode) ? mode : "all");
        return "main/recipe-book";
    }

    // 목록 조회 : 한 번에 8개씩 반환
    @GetMapping("/items")
    @ResponseBody
    public RecipeBookService.BookPage items(@RequestParam(name = "page", defaultValue = "1") long page,
            @RequestParam(name = "keyword", defaultValue = "") String keyword,
            @RequestParam(name = "ingredient", defaultValue = "") String ingredient,
            @RequestParam(name = "category", defaultValue = "") String category,
            @RequestParam(name = "method", defaultValue = "") String method) {
        return service.list(page, keyword, ingredient, category, method);
    }

    @GetMapping("/filters")
    @ResponseBody
    public Map<String, List<String>> filters() { return service.filters(); }

    @GetMapping("/detail/{id}")
    public ResponseEntity<?> detail(@PathVariable("id") String id) {
        SessionInfo info = LoginMemberUtil.getSessionInfo();
        if (info == null || info.getMember_id() == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인 후 레시피 상세를 확인해 주세요."));
        }
        return ResponseEntity.ok(Map.of("recipe", service.detail(id)));
    }
}
