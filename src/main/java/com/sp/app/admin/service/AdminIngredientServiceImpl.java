package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sp.app.admin.domain.dto.AdminIngredientDto;
import com.sp.app.admin.mapper.AdminIngredientMapper;

@Service
public class AdminIngredientServiceImpl implements AdminIngredientService{
	
	@Autowired
	private AdminIngredientMapper mapper;
	
	@Override
	public void insertIngredient(AdminIngredientDto dto) throws Exception {
		try {
			mapper.insertIngredient(dto);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void updateIngredient(AdminIngredientDto dto) throws Exception {
		try {
			mapper.updateIngredient(dto);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public int dataCount(Map<String, Object> map) {
		return mapper.dataCount(map);
	}

	@Override
	public List<AdminIngredientDto> listIngredient(Map<String, Object> map) {
		return mapper.listIngredient(map);
	}

	@Override
	public AdminIngredientDto findById(long ingredientId) {
		return mapper.findById(ingredientId);
	}

	@Override
	public AdminIngredientDto findByName(String ingredientName) {
		return mapper.findByName(ingredientName);
	}
}
