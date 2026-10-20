package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;

public interface TogetherShareService {

    public int insertShare(TogetherShareDto dto) throws Exception;	// 재료나눔글 등록

    public List<TogetherShareDto> listShare(int offset, int size) throws Exception; // 재료나눔 글 목록

    public List<TogetherShareItemDto> searchIngredients(String keyword) throws Exception; // 재료명 / 별칭 검색

    public List<TogetherShareItemDto> listIngredientsByType(String type) throws Exception; // 분류별 재료 목록

    public TogetherShareDto findShareById(Long share_id) throws Exception; // 나눔 글 상세보기

    public List<TogetherShareItemDto> listShareItems(Long share_id) throws Exception; // 상세보기 나눔 재료 목록

    public int updateShare(TogetherShareDto dto) throws Exception; // 재료나눔 글 수정

}
