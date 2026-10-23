package com.sp.app.postgres.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.dto.MyRecipeAiDto;

public interface MyRecipeAiMapper {
	public MyRecipeAiDto findById(@Param("rcp_seq") String rcp_seq);
	
	public List<Map<String, Object>> findBySimilarRecipes( 
		       @Param("embedding") String embedding, @Param("limit") int limit);
}
