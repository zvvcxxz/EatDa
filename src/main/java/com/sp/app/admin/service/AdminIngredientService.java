package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;

import com.sp.app.admin.domain.dto.AdminIngredientDto;

public interface AdminIngredientService {
	public void insertIngredient(AdminIngredientDto dto) throws Exception;
	public void updateIngredient(AdminIngredientDto dto) throws Exception;
	
	public int dataCount(Map<String, Object> map);
	public List<AdminIngredientDto> listIngredient(Map<String, Object> map);
	
	public AdminIngredientDto findById(long ingredientId);
	public AdminIngredientDto findByName(String ingredientName);
}
