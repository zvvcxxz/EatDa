package com.sp.app.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.sp.app.domain.dto.MyRecipeAiDto;
import com.sp.app.service.MyRecipeAiService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/recipes/*")
public class MyRecipeAiController {
	private final MyRecipeAiService service;
	
	@GetMapping("ingredientView")
	public String handleRecipes() {
		// 인그리디언트를 이용한 추천 메인 화면
		return "recipes/ingredientRecipe";
	}
	
	@PostMapping("recommend")
	public ResponseEntity<?> handleRecommend(@RequestBody Map<String, Object> request) {
		try {
	        @SuppressWarnings("unchecked")
	        List<String> ingredients = (List<String>) request.get("ingredients");

	        if (ingredients == null || ingredients.isEmpty()) {
	            return ResponseEntity.badRequest()
	            		.body(Map.of("message", "선택한 재료가 없습니다."));
	        }			
			
			List<Map<String, Object>> recipes = service.recommendRecipes(ingredients);
			
			return ResponseEntity.ok(Map.of("recipes", recipes));
		} catch (Exception e) {
			log.info("handleRecommend : ", e);
			
			 return ResponseEntity.badRequest()
	            		.body(Map.of("message", "레시피 추천 중 오류가 발생했습니다."));
		}
	}

	@GetMapping("searchView")
	public String handleSearchForm() {
		// 레시피 검색을 이용한 추천 메인 화면
		return "recipes/searchRecipe";
	}
	
	@PostMapping("search")
	public ResponseEntity<?> handleSearch(@RequestBody Map<String, String> request) {
		try {
			String keyword = request.get("keyword");
			
			List<Map<String, Object>> recipes = service.recommendRecipes(keyword);
			
			return ResponseEntity.ok(Map.of("recipes", recipes));
		} catch (Exception e) {
			log.info("handleSearch : ", e);
			
			 return ResponseEntity.badRequest()
	            		.body(Map.of("message", "레시피 추천 중 오류가 발생했습니다."));
		}
	}
	
	@GetMapping("{id}")
	public ResponseEntity<?> handleDetailRecommend(@PathVariable("id") String rcp_seq) {
		try {
			MyRecipeAiDto recipe = service.findById(rcp_seq);
			
			Map<String, Object> map = new HashMap<>();
			map.put("recipe", recipe);
			
			return ResponseEntity.ok(map);
		} catch (Exception e) {
			log.info("handleDetailRecommend : ", e);
			
			 return ResponseEntity.badRequest()
	            		.body(Map.of("message", "조리 상세 조회 중 오류가 발생했습니다."));
		}
	}
	
}

