package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;

@Mapper
public interface TogetherShareMapper {

	public int insertShare(TogetherShareDto dto) throws SQLException;	// 재료나눔 글 등록 
	
	public List<TogetherShareItemDto> searchIngredients(@Param("keyword") String keyword);	// 재료명 / 별칭 검색
	
	public List<TogetherShareItemDto> listIngredientsByType(@Param("type") String type);	// 분류별 재료 목록
	
	public int insertShareItem(TogetherShareItemDto dto) throws SQLException; // 재료나눔 게시글 재료등록
}
