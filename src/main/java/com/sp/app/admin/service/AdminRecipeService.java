package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;

import com.sp.app.admin.domain.dto.AdminRecipeDto;

public interface AdminRecipeService {
	int dataCount(Map<String, Object> map);
	List<AdminRecipeDto> listRecipe(Map<String, Object> map);
	
	AdminRecipeDto findById(Long recipeId);
	public void updateRecipeStatus(Map<String, Object> map) throws Exception;
}
