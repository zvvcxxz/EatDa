package com.sp.app.admin.service;


import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sp.app.admin.domain.dto.AdminRecipeDto;
import com.sp.app.admin.mapper.AdminRecipeMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRecipeServiceImpl implements AdminRecipeService{
	private final AdminRecipeMapper mapper;
	
	@Override
	public int dataCount(Map<String, Object> map) {
		int result = 0;
		try {
			result = mapper.dataCount(map);
		} catch (Exception e) {
			log.error("recipe dataCount error", e);
		}
		
		return result;
	}

	@Override
	public List<AdminRecipeDto> listRecipe(Map<String, Object> map) {
		List<AdminRecipeDto> list = null;
		try {
			list = mapper.listRecipe(map);
		} catch (Exception e) {
			log.error("recipe list error", e);
		}
		return list;
	}

	@Override
	public AdminRecipeDto findById(Long recipeId) {
		AdminRecipeDto dto = null;
		try {
			dto = mapper.findById(recipeId);
		} catch (Exception e) {
			log.error("recipe findById error", e);
		}
		
		return dto;
	}

	@Override
	public void updateRecipeStatus(Map<String, Object> map) throws Exception {
		try {
			mapper.updateRecipeStatus(map);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
