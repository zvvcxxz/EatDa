package com.sp.app.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.MyFridgeDto;
import com.sp.app.domain.dto.MyRecipeDto;
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
    @Transactional(readOnly = true)
    public MyFridgeDto.PagedResponse getMemberFridgeItemsFiltered(
            Long memberId, String keyword, String categoryId, int page, int size) {
        
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        String cleanCategory = (categoryId != null && !categoryId.trim().isEmpty()) ? categoryId.trim() : null;

        Pageable pageable = PageRequest.of(page, size);
        
        // 1. 현재 페이지 아이템 조회
        List<MyFridgeDto.ItemResponse> items = memberFridgeRepository.findAllItems(memberId, cleanKeyword, cleanCategory, pageable)
                .stream()
                .map(MyFridgeDto.ItemResponse::new)
                .toList();

        // 2. 전체 개수 조회
        long totalCount = memberFridgeRepository.countFilteredItems(memberId, cleanKeyword, cleanCategory);

        return new MyFridgeDto.PagedResponse(items, totalCount);
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
            Optional<MemberFridge> existingOpt;
            
            // 유통기한 유무에 맞춰 동일 유통기한 항목 탐색
            if (req.getExpiresOn() != null) {
                existingOpt = memberFridgeRepository.findByMemberIdAndIngredient_IdAndExpiresOn(
                    memberId, req.getIngredientId(), req.getExpiresOn());
            } else {
                existingOpt = memberFridgeRepository.findByMemberIdAndIngredient_IdAndExpiresOnIsNull(
                    memberId, req.getIngredientId());
            }

            if (existingOpt.isPresent()) {
                MemberFridge existingItem = existingOpt.get();
                
                // 기존 수량 + 신규 수량 계산 (null 안전 처리)
                BigDecimal addedQuantity = (req.getQuantity() != null) ? req.getQuantity() : BigDecimal.ONE;
                BigDecimal currentQuantity = (existingItem.getQuantity() != null) ? existingItem.getQuantity() : BigDecimal.ZERO;
                BigDecimal newQuantity = currentQuantity.add(addedQuantity);

                // Setter 대신 엔티티의 updateDetails 메서드 호출
                existingItem.updateDetails(newQuantity, req.getUnit(), req.getExpiresOn(), req.getMemo());
            } else {
                // 유통기한이 다르면 새로운 카드(행)로 등록
                MyIngredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));
                
                MemberFridge newItem = MemberFridge.builder()
                    .memberId(memberId)
                    .ingredient(ingredient)
                    .quantity(req.getQuantity())
                    .unit(req.getUnit())
                    .expiresOn(req.getExpiresOn())
                    .memo(req.getMemo())
                    .build();
                    
                memberFridgeRepository.save(newItem);
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

    //보유 재료 기반 가중치 레시피 추천
    @Override
    public List<MyRecipeRecommendDto> getRecommendedRecipes(Long memberId) {
        // 1. MyBatis 가중치 추천 쿼리로 상위 10개 레시피 기본 정보 조회
        List<MyRecipeRecommendDto> recommendations = recipeMapper.findRecommendedRecipes(memberId, 10);

        if (recommendations == null || recommendations.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 사용자의 냉장고 보유 재료 목록 조회 (JPA)
        List<MemberFridge> myFridgeItems = memberFridgeRepository.findAllByMemberIdWithIngredient(memberId);

        // 빠른 비교를 위한 보유 재료 ID Set 및 이름 Set 생성
        Set<Long> myIngredientIds = myFridgeItems.stream()
                .filter(mf -> mf.getIngredient() != null)
                .map(mf -> mf.getIngredient().getId())
                .collect(Collectors.toSet());

        Set<String> myIngredientNames = myFridgeItems.stream()
                .filter(mf -> mf.getIngredient() != null && mf.getIngredient().getName() != null)
                .map(mf -> mf.getIngredient().getName().trim())
                .collect(Collectors.toSet());

        // 3. 각 추천 레시피별로 필요한 재료 목록을 조회하여 보유/부족 재료 분류 및 일치율 계산
        for (MyRecipeRecommendDto recipeDto : recommendations) {
            List<MyRecipeDto.IngredientItem> recipeIngredients = 
                    recipeMapper.selectRecipeIngredients(recipeDto.getRecipeId());

            List<String> matchingList = new ArrayList<>();
            List<String> missingList = new ArrayList<>();

            if (recipeIngredients != null && !recipeIngredients.isEmpty()) {
                for (MyRecipeDto.IngredientItem item : recipeIngredients) {
                    String ingredientName = item.getIngredientName();
                    Long ingredientId = item.getIngredientId();

                    // ID 또는 이름 기준으로 보유 여부 검증
                    boolean isOwned = (ingredientId != null && myIngredientIds.contains(ingredientId))
                            || (ingredientName != null && myIngredientNames.contains(ingredientName.trim()));

                    if (isOwned) {
                        matchingList.add(ingredientName);
                    } else {
                        missingList.add(ingredientName);
                    }
                }

                // 일치율 (%) 계산 = (보유 재료 수 / 레시피 전체 재료 수) * 100
                int totalRecipeIngCount = recipeIngredients.size();
                double matchRate = ((double) matchingList.size() / totalRecipeIngCount) * 100.0;
                
                // 소수점 첫째 자리 반올림
                recipeDto.setMatchRate(Math.round(matchRate * 10.0) / 10.0);
            } else {
                recipeDto.setMatchRate(0.0);
            }

            // DTO 필드 세팅
            recipeDto.setMatchingIngredients(matchingList);
            recipeDto.setMissingIngredients(missingList);
        }
        recommendations.sort((a, b) -> Double.compare(b.getMatchRate(), a.getMatchRate()));

        return recommendations;
    }
}