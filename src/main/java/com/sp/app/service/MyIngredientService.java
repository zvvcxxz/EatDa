package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.MyIngredientSearchDto;

public interface MyIngredientService {
	public List<MyIngredientSearchDto> searchIngredients(String keyword);
}
