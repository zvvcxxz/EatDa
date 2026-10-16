package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.MyRecipeDto;

public interface MyRecipeService {
	
    List<MyRecipeDto> selectMyRecipes(Long memberId, int offset, int size);  
    long countMyRecipes(Long memberId);

    List<MyRecipeDto> selectBookmarkedRecipes(Long memberId, int offset, int size);  
    long countBookmarkedRecipes(Long memberId);
}
