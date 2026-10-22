package com.sp.app.admin.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.admin.domain.dto.AdminIngredientDto;

@Mapper
public interface AdminIngredientMapper {
	public void insertIngredient(AdminIngredientDto dto) throws SQLException;
	public void updateIngredient(AdminIngredientDto dto) throws SQLException;
	
	public int dataCount(Map<String, Object> map);
	public List<AdminIngredientDto> listIngredient(Map<String, Object> map);
	
	public AdminIngredientDto findById(long ingredientId);
	public AdminIngredientDto findByName(String ingredientName);
}
