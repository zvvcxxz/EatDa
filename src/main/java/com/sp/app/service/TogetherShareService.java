package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;

public interface TogetherShareService {
	
	public int insertShare(TogetherShareDto dto) throws Exception;	// 재료나눔글 등록
	
	public List<TogetherShareItemDto> searchIngredients(String keyword) throws Exception; // 재료명 / 별칭 검색
	
	public List<TogetherShareItemDto> listIngredientsByType(String type) throws Exception; // 분류별 재료 목록
	
}
