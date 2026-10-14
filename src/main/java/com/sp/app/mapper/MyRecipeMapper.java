package com.sp.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.sp.app.domain.dto.MyRecipeDto;
import com.sp.app.domain.dto.MyRecipeRecommendDto;

@Mapper
public interface MyRecipeMapper {

	// 개별 서브 조회 메서드
    MyRecipeDto findRecipeDetailById(@Param("recipeId") Long recipeId);
    List<MyRecipeDto.IngredientItem> selectRecipeIngredients(@Param("recipeId") Long recipeId);
    List<MyRecipeDto.StepItem> selectRecipeSteps(@Param("recipeId") Long recipeId);
    List<MyRecipeDto.CategoryItem> selectCategoriesByRecipeId(@Param("recipeId") Long recipeId);
    List<MyRecipeDto.TagItem> selectRecipeTags(@Param("recipeId") Long recipeId);
    List<MyRecipeDto.ReviewItem> selectRecipeReviews(@Param("recipeId") Long recipeId);

    // 레시피 추천
    List<MyRecipeRecommendDto> findRecommendedRecipes(
            @Param("memberId") Long memberId,
            @Param("limit") int limit
    );

}
