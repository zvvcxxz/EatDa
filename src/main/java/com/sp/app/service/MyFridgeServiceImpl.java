package com.sp.app.service;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    
    	//unit이 사용자 입력이 가능하기때문에 방어선 짜기
    private String validateAndCleanUnit(String unit) {
    	
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("단위를 입력해 주세요.");
        }
        String cleanUnit = unit.trim();
        
        if (cleanUnit.length() > 6) {
            throw new IllegalArgumentException("단위는 최대 6자까지만 입력 가능합니다.");
        }

        if (!cleanUnit.matches("^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ\\s/.~]+$")) {
            throw new IllegalArgumentException("단위에 허용되지 않는 특수문자나 스크립트가 포함되어 있습니다.");
        }

        return cleanUnit;
    }

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
    		if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("등록할 재료 정보가 없습니다.");
        }    
    	
    		for (MyFridgeDto.RegisterRequest req : requests) {
    			String cleanUnit = validateAndCleanUnit(req.getUnit());
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
                existingItem.updateDetails(newQuantity, cleanUnit, req.getExpiresOn(), req.getMemo());
            } else {
                // 유통기한이 다르면 새로운 카드(행)로 등록
                MyIngredient ingredient = ingredientRepository.findById(req.getIngredientId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 재료입니다."));
                
                MemberFridge newItem = MemberFridge.builder()
                    .memberId(memberId)
                    .ingredient(ingredient)
                    .quantity(req.getQuantity())
                    .unit(cleanUnit)
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
    		if (request == null) {
            throw new IllegalArgumentException("수정할 정보가 올바르지 않습니다.");
        }
    		String cleanUnit = validateAndCleanUnit(request.getUnit());
    		MemberFridge fridgeItem = memberFridgeRepository.findByIdAndMemberId(fridgeItemId, memberId)
                .orElseThrow(() -> new IllegalArgumentException("냉장고 재료를 찾을 수 없습니다. ID: " + fridgeItemId));

        fridgeItem.updateDetails(request.getQuantity(), cleanUnit, request.getExpiresOn(), request.getMemo());
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
        //가중치 추천 상위 10개 레시피
        List<MyRecipeRecommendDto> recommendations = recipeMapper.findRecommendedRecipes(memberId, 10);

        if (recommendations == null || recommendations.isEmpty()) {
            return Collections.emptyList();
        }

        List<MemberFridge> myFridgeItems = memberFridgeRepository.findAllByMemberIdWithIngredient(memberId);

        Set<Long> myIngredientIds = myFridgeItems.stream()
                .filter(mf -> mf.getIngredient() != null)
                .map(mf -> mf.getIngredient().getId())
                .collect(Collectors.toSet());

        Set<String> myIngredientNames = myFridgeItems.stream()
                .filter(mf -> mf.getIngredient() != null && mf.getIngredient().getName() != null)
                .map(mf -> mf.getIngredient().getName().trim())
                .collect(Collectors.toSet());
        
        //임박&경과 재료
        LocalDate warningDate = LocalDate.now().plusDays(3);
        Set<String> urgentIngredientNames = myFridgeItems.stream()
                .filter(mf -> mf.getIngredient() != null && mf.getIngredient().getName() != null)
                .filter(mf -> mf.getExpiresOn() != null && !mf.getExpiresOn().isAfter(warningDate)) // 유통기한 <= 오늘 + 3일
                .map(mf -> mf.getIngredient().getName().trim())
                .collect(Collectors.toSet());

        //보유/부족 재료 분류 및 일치율 계산
        for (MyRecipeRecommendDto recipeDto : recommendations) {
            List<MyRecipeDto.IngredientItem> recipeIngredients = 
                    recipeMapper.selectRecipeIngredients(recipeDto.getRecipeId());

            List<String> matchingList = new ArrayList<>();
            List<String> missingList = new ArrayList<>();
            List<String> urgentList = new ArrayList<>();

            if (recipeIngredients != null && !recipeIngredients.isEmpty()) {
                for (MyRecipeDto.IngredientItem item : recipeIngredients) {
                    String ingredientName = item.getIngredientName();
                    Long ingredientId = item.getIngredientId();
                    		//아이디로 보유여부 검증
                    boolean isOwned = (ingredientId != null && myIngredientIds.contains(ingredientId))
                            || (ingredientName != null && myIngredientNames.contains(ingredientName.trim()));

                    if (isOwned) {
                        matchingList.add(ingredientName);
                        
                        //보유 재료 중 임박 재료에 해당하는 경우 별도 수집
                        if (ingredientName != null && urgentIngredientNames.contains(ingredientName.trim())) {
                            urgentList.add(ingredientName);
                        }
                    } else {
                        missingList.add(ingredientName);
                    }
                }

                int totalRecipeIngCount = recipeIngredients.size();
                double matchRate = ((double) matchingList.size() / totalRecipeIngCount) * 100.0;
                recipeDto.setMatchRate(Math.round(matchRate * 10.0) / 10.0);
            } else {
                recipeDto.setMatchRate(0.0);
            }

            recipeDto.setMatchingIngredients(matchingList);
            recipeDto.setMissingIngredients(missingList);
            recipeDto.setUrgentIngredients(urgentList);
        }
        //recommendations.sort((a, b) -> Double.compare(b.getMatchRate(), a.getMatchRate()));

        return recommendations;
    }
}