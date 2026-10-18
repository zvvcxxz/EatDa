package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.MyRecipeDto;

public interface MyRecipeService {
	
    List<MyRecipeDto> selectMyRecipes(Long memberId, int offset, int size, String sort);  
    long countMyRecipes(Long memberId);

    List<MyRecipeDto> selectBookmarkedRecipes(Long memberId, int offset, int size, String sort);  
    long countBookmarkedRecipes(Long memberId);
}
