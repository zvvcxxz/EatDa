package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.CommunityCategoryDto;
import com.sp.app.domain.dto.CommunityIngredientDto;
import com.sp.app.domain.dto.CommunityRecipeDto;
import com.sp.app.domain.dto.CommunityRecipeIngredientDto;
import com.sp.app.domain.dto.CommunityRecipeStepDto;
import com.sp.app.domain.dto.CommunityTagDto;

@Mapper
public interface CommunityRecipeMapper {

    // 레시피 목록·검색·상세
    public int dataCount(Map<String, Object> map);
    public List<CommunityRecipeDto> listRecipe(Map<String, Object> map);
    public CommunityRecipeDto findById(long recipeId);
    
    // 검색 조건에 맞는 이전글·다음글
    public CommunityRecipeDto findByPrev(Map<String, Object> map);
    public CommunityRecipeDto findByNext(Map<String, Object> map);

    // 레시피 기본 정보 저장
    public void insertRecipe(CommunityRecipeDto dto) throws SQLException;
    public int updateRecipe(CommunityRecipeDto dto) throws SQLException;
    public int deleteRecipe(Map<String, Object> map) throws SQLException;
    public int hideRecipe(Map<String, Object> map) throws SQLException;

    // 등록 화면에서 선택할 카테고리·태그·재료 조회
    public List<CommunityCategoryDto> listCategory();
    public List<CommunityTagDto> listTag();
    public List<CommunityIngredientDto> listIngredient(Map<String, Object> map);

    // 선택된 카테고리와 태그 번호
    public List<Long> listCategoryIds(long recipeId);
    public List<Long> listTagIds(long recipeId);

    // 레시피와 카테고리·태그 연결
    public void insertRecipeCategory(Map<String, Object> map) throws SQLException;
    public void insertRecipeTag(Map<String, Object> map) throws SQLException;
    public void deleteRecipeCategory(long recipeId) throws SQLException;
    public void deleteRecipeTag(long recipeId) throws SQLException;

    // 레시피 재료와 분량
    public List<CommunityRecipeIngredientDto> listRecipeIngredient(long recipeId);
    public void insertRecipeIngredient(CommunityRecipeIngredientDto dto) throws SQLException;
    public void deleteRecipeIngredient(long recipeId) throws SQLException;

    // 조리 순서와 단계 사진
    public List<CommunityRecipeStepDto> listRecipeStep(long recipeId);
    public void insertRecipeStep(CommunityRecipeStepDto dto) throws SQLException;
    public void deleteRecipeStep(long recipeId) throws SQLException;

    // 좋아요 등록·취소·개수·본인 선택 여부
    public void insertRecipeLike(Map<String, Object> map) throws SQLException;
    public int deleteRecipeLike(Map<String, Object> map) throws SQLException;
    public int recipeLikeCount(long recipeId);
    public int hasRecipeLike(Map<String, Object> map);

    // 찜 등록·취소·개수·본인 선택 여부
    public void insertRecipeBookmark(Map<String, Object> map) throws SQLException;
    public int deleteRecipeBookmark(Map<String, Object> map) throws SQLException;
    public int recipeBookmarkCount(long recipeId);
    public int hasRecipeBookmark(Map<String, Object> map);
}