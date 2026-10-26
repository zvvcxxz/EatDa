package com.sp.app.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.sp.app.domain.dto.CommunityRecipeDto;
import com.sp.app.domain.dto.CommunityReviewDto;
import com.sp.app.service.CommunityRecipeService;
import com.sp.app.service.CommunityReviewService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeRecipeSlideController {

	private final CommunityRecipeService recipeService;
	private final CommunityReviewService reviewService;

	// 메인 슬라이드 : 모두의 레시피 목록 조회
	@GetMapping("/home/recipe-slides")
	public ResponseEntity<?> getRecipeSlide() {

		Map<String, Object> map = Map.of(
				"offset" , 0,
				"size", 6
				);

		List<CommunityRecipeDto> recipes = recipeService.listRecipe(map);

		return ResponseEntity.ok(recipes);
	}

	// 메인 슬라이드 : 따라 만든 요리 목록 조회
	@GetMapping("/home/cooking-slides")
	public ResponseEntity<?> getCookingSlide() {

		Map<String, Object> map = Map.of(
				"offset" , 0,
				"size" , 6
				);

		List<CommunityReviewDto> reviews =
				reviewService.listReview(map);

		return ResponseEntity.ok(reviews);
	}
}
