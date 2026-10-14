package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityCategoryDto;
import com.sp.app.domain.dto.CommunityIngredientDto;
import com.sp.app.domain.dto.CommunityRecipeDto;
import com.sp.app.domain.dto.CommunityRecipeIngredientDto;
import com.sp.app.domain.dto.CommunityRecipeStepDto;
import com.sp.app.domain.dto.CommunityTagDto;

public interface CommunityRecipeService {

    // 레시피 목록·검색·상세
    public int dataCount(Map<String, Object> map);
    public List<CommunityRecipeDto> listRecipe(Map<String, Object> map);
    public CommunityRecipeDto findById(long recipeId);
    
    // 검색 조건에 맞는 이전글·다음글
    public CommunityRecipeDto findByPrev(Map<String, Object> map);
    public CommunityRecipeDto findByNext(Map<String, Object> map);

    // 기본 정보와 재료·조리 단계를 함께 저장
    public void insertRecipe(CommunityRecipeDto dto,
            List<CommunityRecipeIngredientDto> ingredients,
            List<CommunityRecipeStepDto> steps) throws Exception;

    public int updateRecipe(CommunityRecipeDto dto,
            List<CommunityRecipeIngredientDto> ingredients,
            List<CommunityRecipeStepDto> steps) throws Exception;

    public int deleteRecipe(Map<String, Object> map) throws Exception;
    public int hideRecipe(Map<String, Object> map) throws Exception;

    // 등록 화면에서 선택할 목록
    public List<CommunityCategoryDto> listCategory();
    public List<CommunityTagDto> listTag();
    public List<CommunityIngredientDto> listIngredient(Map<String, Object> map);

    // 상세·수정 화면의 재료와 조리 단계
    public List<CommunityRecipeIngredientDto> listRecipeIngredient(long recipeId);
    public List<CommunityRecipeStepDto> listRecipeStep(long recipeId);

    // 좋아요 등록·취소·개수·본인 선택 여부
    public void insertRecipeLike(Map<String, Object> map) throws Exception;
    public int deleteRecipeLike(Map<String, Object> map) throws Exception;
    public int recipeLikeCount(long recipeId);
    public int hasRecipeLike(Map<String, Object> map);

    // 찜 등록·취소·개수·본인 선택 여부
    public void insertRecipeBookmark(Map<String, Object> map) throws Exception;
    public int deleteRecipeBookmark(Map<String, Object> map) throws Exception;
    public int recipeBookmarkCount(long recipeId);
    public int hasRecipeBookmark(Map<String, Object> map);
}