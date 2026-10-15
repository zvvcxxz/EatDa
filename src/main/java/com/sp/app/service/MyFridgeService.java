package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.MyFridgeDto;
import com.sp.app.domain.dto.MyRecipeRecommendDto;

public interface MyFridgeService {
	// 1. 재료 DB 사전 조회
    List<MyFridgeDto.IngredientResponse> searchIngredients(String query);
    List<MyFridgeDto.IngredientResponse> getIngredientsByCategory(String categoryCode);

    // 2. 내 냉장고 재료 관리 (CRUD)
    MyFridgeDto.PagedResponse getMemberFridgeItemsFiltered(Long memberId, String keyword, String categoryId, int page, int size);
    List<MyFridgeDto.ItemResponse> getMemberFridgeItems(Long memberId);
    void registerFridgeItems(Long memberId, List<MyFridgeDto.RegisterRequest> requests);
    void updateFridgeItem(Long memberId, Long fridgeItemId, MyFridgeDto.UpdateRequest request);
    void deleteFridgeItem(Long memberId, Long fridgeItemId);

    // 3. 보유 재료 기반 가중치 레시피 추천
    List<MyRecipeRecommendDto> getRecommendedRecipes(Long memberId);

}