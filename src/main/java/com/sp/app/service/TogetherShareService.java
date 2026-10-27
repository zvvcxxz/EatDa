package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.TogetherShareDto;
import com.sp.app.domain.dto.TogetherShareItemDto;

public interface TogetherShareService {

    // 재료 나눔 : 나눔 글과 재료 저장

    public int insertShare(TogetherShareDto dto) throws Exception; // 재료나눔글 등록

    // 재료 나눔 : 나눔 글 목록 조회

    public List<TogetherShareDto> listShare(int offset, int size) throws Exception; // 재료나눔 글 목록

    // 재료 나눔 : 검색어에 맞는 재료 조회

    public List<TogetherShareItemDto> searchIngredients(String keyword) throws Exception; // 재료명 / 별칭 검색

    // 재료 나눔 : 나눔 유형에 맞는 재료 조회

    public List<TogetherShareItemDto> listIngredientsByType(String type) throws Exception; // 분류별 재료 목록

    // 재료 나눔 : 나눔 글 상세 조회

    public TogetherShareDto findShareById(Long share_id) throws Exception; // 나눔 글 상세보기

    // 재료 나눔 : 게시글의 나눔 재료 조회

    public List<TogetherShareItemDto> listShareItems(Long share_id) throws Exception; // 상세보기 나눔 재료 목록

    // 재료 나눔 : 나눔 글과 재료 수정

    public int updateShare(TogetherShareDto dto) throws Exception; // 재료나눔 글 수정

    // 재료 나눔 : 나눔 글 삭제

    public int deleteShare(Long share_id, Long author_id) throws Exception; // 재료나눔 글 삭제

}
