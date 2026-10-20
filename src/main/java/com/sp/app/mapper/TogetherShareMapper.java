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

    public List<TogetherShareDto> listShare(@Param("offset") int offset, @Param("size") int size) throws SQLException; // 재료나눔 글 목록

    public List<TogetherShareItemDto> searchIngredients(@Param("keyword") String keyword);	// 재료명 / 별칭 검색

    public List<TogetherShareItemDto> listIngredientsByType(@Param("type") String type);	// 분류별 재료 목록

    public int insertShareItem(TogetherShareItemDto dto) throws SQLException; // 재료나눔 게시글 재료등록

    public TogetherShareDto findShareById(@Param("share_id") Long share_id) throws Exception; // 재료나눔 글 상세보기

    public List<TogetherShareItemDto> listShareItems(@Param("share_id") Long share_id) throws Exception; // 해당 글의 나눔 재료 목록

    public int updateShare(TogetherShareDto dto) throws Exception; // 재료나눔 글 수정

    public int updateShareItem(TogetherShareItemDto dto) throws Exception; // 기존 나눔 재료 수정

    public int deleteShareItem(@Param("share_id") Long share_id, @Param("share_item_id") Long share_item_id) throws Exception; // 나눔 글에서 재료 삭제
}
