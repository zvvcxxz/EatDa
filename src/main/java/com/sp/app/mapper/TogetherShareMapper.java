package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;

@Mapper
public interface TogetherShareMapper {

    // 재료 나눔 : 나눔 글과 재료 저장

    public int insertShare(TogetherShareDto dto) throws SQLException; // 재료나눔 글 등록

    // 재료 나눔 : 나눔 글 목록 조회

    public List<TogetherShareDto> listShare(@Param("offset") int offset, @Param("size") int size)
            throws SQLException; // 재료나눔 글 목록

    // 재료 나눔 : 검색어에 맞는 재료 조회

    public List<TogetherShareItemDto> searchIngredients(@Param("keyword") String keyword); // 재료명 / 별칭 검색

    // 재료 나눔 : 나눔 유형에 맞는 재료 조회

    public List<TogetherShareItemDto> listIngredientsByType(@Param("type") String type); // 분류별 재료 목록

    // 재료 나눔 : 나눔 글에 재료 저장

    public int insertShareItem(TogetherShareItemDto dto) throws SQLException; // 재료나눔 게시글 재료등록

    // 재료 나눔 : 나눔 글 상세 조회

    public TogetherShareDto findShareById(@Param("share_id") Long share_id) throws Exception; // 재료나눔 글 상세보기

    // 재료 나눔 : 게시글의 나눔 재료 조회

    public List<TogetherShareItemDto> listShareItems(@Param("share_id") Long share_id) throws Exception; // 해당 글의 나눔 재료 목록

    // 재료 나눔 : 나눔 글과 재료 수정

    public int updateShare(TogetherShareDto dto) throws Exception; // 재료나눔 글 수정

    // 재료 나눔 : 나눔 재료 수정

    public int updateShareItem(TogetherShareItemDto dto) throws Exception; // 기존 나눔 재료 수정

    // 재료 나눔 : 기존 나눔 재료 삭제

    public int deleteShareItem(@Param("share_id") Long share_id, @Param("share_item_id") Long share_item_id)
            throws Exception; // 나눔 글에서 재료 삭제

    // 재료 나눔 : 나눔 글 삭제

    public int deleteShare(@Param("share_id") Long share_id, @Param("author_id") Long author_id)
            throws Exception; // 재료나눔 글 삭제
}
