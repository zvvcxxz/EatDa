package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.MyRecipeAiDto;

public interface MyRecipeAiService {
	public MyRecipeAiDto findById(String rcp_seq);
	public List<Map<String, Object>> recommendRecipes(List<String> ingredients);
	public List<Map<String, Object>> recommendRecipes(String keyword); // 승호가 써야할거
	
}
