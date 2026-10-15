package com.sp.app.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sp.app.domain.dto.MyFridgeDto;
import com.sp.app.domain.dto.MyRecipeRecommendDto;
import com.sp.app.domain.dto.SessionInfo;
import com.sp.app.security.LoginMemberUtil;
import com.sp.app.service.MyFridgeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fridge")
@RequiredArgsConstructor
public class MyFridgeApiController {

    private final MyFridgeService fridgeService;

    // 로그인 회원 ID 추출 헬퍼 메서드
    private Long getLoginMemberId() {
        SessionInfo info = LoginMemberUtil.getSessionInfo();
        if (info == null || info.getMember_id() == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }
        return info.getMember_id();
    }

    // 1. 재료 연관 검색어 (재료명 및 동의어 검색)
    @GetMapping("/ingredients/search")
    public ResponseEntity<List<MyFridgeDto.IngredientResponse>> searchIngredients(
            @RequestParam(value = "query", required = false) String query) {
        List<MyFridgeDto.IngredientResponse> result = fridgeService.searchIngredients(query);
        return ResponseEntity.ok(result);
    }

    // 2. 카테고리별 재료 목록 조회
    @GetMapping("/ingredients/by-category")
    public ResponseEntity<List<MyFridgeDto.IngredientResponse>> getIngredientsByCategory(
            @RequestParam("category") String category) {
        List<MyFridgeDto.IngredientResponse> result = fridgeService.getIngredientsByCategory(category);
        return ResponseEntity.ok(result);
    }

    // 3. 내 냉장고 보유 재료 목록 조회 (페이징 처리)
    /*@GetMapping("/items")
    public ResponseEntity<MyFridgeDto.PagedResponse> getFridgeItems(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) String categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        Long memberId = getLoginMemberId();
        MyFridgeDto.PagedResponse response = fridgeService.getMemberFridgeItemsFiltered(
                memberId, keyword, categoryId, page, size);
                
        return ResponseEntity.ok(response);
    }*/
    // 내 냉장고 보유 재료 전체 조회: 페이징 처리 하니까 오래된/유통기한 임박/경과한 재료 필터링이 제대로 안 되는 문제가 있음
    	//해결하려면 너무 쿼리가 복잡해지고 유저의 냉장고 재료 조회가 크게 db를 무겁게 만들지 않으므로 그냥 js로 페이징처리
    @GetMapping("/items")
    public ResponseEntity<List<MyFridgeDto.ItemResponse>> getFridgeItems() {
        Long memberId = getLoginMemberId();
        List<MyFridgeDto.ItemResponse> items = fridgeService.getMemberFridgeItems(memberId);
        return ResponseEntity.ok(items);
    }
    

    // 4. 내 냉장고에 재료 등록 (일괄 등록 및 중복 수량 합산)
    @PostMapping("/items")
    public ResponseEntity<Void> registerFridgeItems(@RequestBody List<MyFridgeDto.RegisterRequest> requests) {
        Long memberId = getLoginMemberId();
        fridgeService.registerFridgeItems(memberId, requests);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 5. 내 냉장고 재료 정보 수정 (수량, 단위, 유통기한 등)
    @PutMapping("/items/{fridgeItemId}")
    public ResponseEntity<Void> updateFridgeItem(@PathVariable("fridgeItemId") Long fridgeItemId,
                                                  @RequestBody MyFridgeDto.UpdateRequest request) {
        Long memberId = getLoginMemberId();
        fridgeService.updateFridgeItem(memberId, fridgeItemId, request);
        return ResponseEntity.ok().build();
    }

    // 6. 내 냉장고 재료 삭제
    @DeleteMapping("/items/{fridgeItemId}")
    public ResponseEntity<Void> deleteFridgeItem(@PathVariable("fridgeItemId") Long fridgeItemId) {
        Long memberId = getLoginMemberId();
        fridgeService.deleteFridgeItem(memberId, fridgeItemId);
        return ResponseEntity.ok().build();
    }

    // 7. 보유 재료 기반 가중치 레시피 추천 API
    @GetMapping("/recipes/recommend")
    public ResponseEntity<List<MyRecipeRecommendDto>> getRecommendedRecipes() {
        Long memberId = getLoginMemberId();
        List<MyRecipeRecommendDto> recommendations = fridgeService.getRecommendedRecipes(memberId);
        return ResponseEntity.ok(recommendations);
    }
}
