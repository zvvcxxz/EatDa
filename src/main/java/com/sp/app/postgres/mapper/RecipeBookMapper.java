package com.sp.app.postgres.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.sp.app.domain.dto.MyRecipeAiDto;
import com.sp.app.domain.dto.RecipeBookItemDto;

// 레시피북 : PostgreSQL 조회 전용
public interface RecipeBookMapper {
    long countRecipes(@Param("keyword") String keyword, @Param("ingredient") String ingredient,
            @Param("category") String category, @Param("method") String method);

    List<RecipeBookItemDto> listRecipes(@Param("keyword") String keyword,
            @Param("ingredient") String ingredient, @Param("category") String category,
            @Param("method") String method, @Param("offset") long offset, @Param("limit") int limit);

    List<String> listCategories();
    List<String> listMethods();
    MyRecipeAiDto findRecipe(@Param("id") String id);
}
