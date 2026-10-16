package com.sp.app.admin.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.admin.domain.dto.AdminRecipeDto;

@Mapper
public interface AdminRecipeMapper {
	int dataCount(Map<String, Object> map);
    List<AdminRecipeDto> listRecipe(Map<String, Object> map);
    AdminRecipeDto findById(Long recipeId);
    
    public void updateRecipeStatus(Map<String, Object> map) throws Exception;
}
