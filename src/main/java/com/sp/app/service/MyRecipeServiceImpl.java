package com.sp.app.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.MyRecipeDto;
import com.sp.app.mapper.MyRecipeMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyRecipeServiceImpl implements MyRecipeService {

	private final MyRecipeMapper myRecipeMapper;
	
	@Override
	public List<MyRecipeDto> selectMyRecipes(Long memberId, int offset, int size, String sort) {
		List<MyRecipeDto> list = null;
		list = myRecipeMapper.selectMyRecipes(memberId, offset, size, sort);
		
		return list;
	}

	@Override
	public long countMyRecipes(Long memberId) {
		long count = 0;
		count = myRecipeMapper.countMyRecipes(memberId);
		return count;
	}

	@Override
	public List<MyRecipeDto> selectBookmarkedRecipes(Long memberId, int offset, int size, String sort) {
		List<MyRecipeDto> list = null;
		list = myRecipeMapper.selectBookmarkedRecipes(memberId, offset, size, sort);

		return list;
	}

	@Override
	public long countBookmarkedRecipes(Long memberId) {
		long count = 0;
		count = myRecipeMapper.countBookmarkedRecipes(memberId);
		return count;
	}

}
