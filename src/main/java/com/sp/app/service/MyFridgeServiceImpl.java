package com.sp.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.MyFridgeDto;
import com.sp.app.domain.dto.MyRecipeRecommendDto;
import com.sp.app.domain.entity.MemberFridge;
import com.sp.app.domain.entity.MyIngredient;
import com.sp.app.mapper.MyRecipeMapper;
import com.sp.app.repository.MemberFridgeRepository;
import com.sp.app.repository.MyIngredientRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyFridgeServiceImpl implements MyFridgeService {

    private final MyIngredientRepository ingredientRepository;
    private final MemberFridgeRepository memberFridgeRepository;
    private final MyRecipeMapper recipeMapper;

    // 재료 DB 사전 조회
    @Override
    public List<MyFridgeDto.IngredientResponse> searchIngredients(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        return ingredientRepository.searchByNameOrAlias(query.trim())
                .stream()
                .map(MyFridgeDto.IngredientResponse::new)
                .toList();
    }

    @Override
    public List<MyFridgeDto.IngredientResponse> getIngredientsByCategory(String categoryCode) {
        return ingredientRepository.findByTypeAndActiveYn(categoryCode, "Y")
                .stream()
                .map(MyFridgeDto.IngredientResponse::new)
                .toList();
    }

    //2. 회원 냉장고 재료 관리

    @Override
    public List<MyFridgeDto.ItemResponse> getMemberFridgeItemsFiltered(Long memberId, String keyword, String categoryId, int limit) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanCategory = (categoryId != null && !categoryId.trim().isEmpty()) ? categoryId.trim() : null;

        Pageable pageable = PageRequest.of(0, limit);
        return memberFridgeRepository.findTop30Items(memberId, cleanKeyword, cleanCategory, pageable)
                .stream()
                .map(MyFridgeDto.ItemResponse::new)
                .toList();
    }

    @Override
    public List<MyFridgeDto.ItemResponse> getMemberFridgeItems(Long memberId) {
        return memberFridgeRepository.findAllByMemberIdWithIngredient(memberId)
                .stream()
                .map(MyFridgeDto.ItemResponse::new)
                .toList();
    }

    @Override
    @Transactional
    public void registerFridgeItems(Long memberId, List<MyFridgeDto.RegisterRequest> requests) {
        for (MyFridgeDto.RegisterRequest req : requests) {
            MyIngredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료 ID: " + req.getIngredientId()));

            // 중복 처리: 이미 냉장고에 해당 재료가 있는 경우 기존 데이터에 수량 합산
            Optional<MemberFridge> existingItemOpt = memberFridgeRepository.findByMemberIdAndIngredient_Id(memberId, req.getIngredientId());

            if (existingItemOpt.isPresent()) {
                MemberFridge existingItem = existingItemOpt.get();
                var newQuantity = (existingItem.getQuantity() != null ? existingItem.getQuantity() : java.math.BigDecimal.ZERO)
                        .add(req.getQuantity() != null ? req.getQuantity() : java.math.BigDecimal.ONE);
                
                existingItem.updateDetails(newQuantity, req.getUnit(), req.getExpiresOn(), req.getMemo());
            } else {
                MemberFridge memberFridge = MemberFridge.builder()
                        .memberId(memberId)
                        .ingredient(ingredient)
                        .quantity(req.getQuantity())
                        .unit(req.getUnit() != null ? req.getUnit() : ingredient.getDefaultUnit())
                        .expiresOn(req.getExpiresOn())
                        .memo(req.getMemo())
                        .build();

                memberFridgeRepository.save(memberFridge);
            }
        }
    }

    @Override
    @Transactional
    public void updateFridgeItem(Long memberId, Long fridgeItemId, MyFridgeDto.UpdateRequest request) {
        MemberFridge fridgeItem = memberFridgeRepository.findByIdAndMemberId(fridgeItemId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("냉장고 재료를 찾을 수 없습니다. ID: " + fridgeItemId));

        fridgeItem.updateDetails(request.getQuantity(), request.getUnit(), request.getExpiresOn(), request.getMemo());
    }

    @Override
    @Transactional
    public void deleteFridgeItem(Long memberId, Long fridgeItemId) {
        MemberFridge fridgeItem = memberFridgeRepository.findByIdAndMemberId(fridgeItemId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 재료를 찾을 수 없습니다. ID: " + fridgeItemId));

        memberFridgeRepository.delete(fridgeItem);
    }

    //3. 보유 재료 기반 가중치 레시피 추천
    @Override
    public List<MyRecipeRecommendDto> getRecommendedRecipes(Long memberId) {
        // 1. 회원의 냉장고에 등록된 재료가 있는지 먼저 확인 (없으면 추천 안함)
        List<MyFridgeDto.ItemResponse> fridgeItems = getMemberFridgeItemsFiltered(memberId, null, null, 1);
        if (fridgeItems.isEmpty()) {
            return List.of();
        }

        // 2. MyBatis 쿼리를 통해 가중치(임박 재료 2.5점 / 일반 재료 1점) 계산된 상위 10개 레시피 추천
        return recipeMapper.findRecommendedRecipes(memberId, 10);
    }
}