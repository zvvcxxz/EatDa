package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityPostDto;

public interface CommunityPostService {

    // 게시글 등록·수정·삭제
    public void insertPost(CommunityPostDto dto) throws Exception;
    public int updatePost(CommunityPostDto dto) throws Exception;
    public int deletePost(Map<String, Object> map) throws Exception;
    public int hidePost(long postId) throws Exception;

    // 목록과 상세 조회
    public int dataCount(Map<String, Object> map);
    public List<CommunityPostDto> listPost(Map<String, Object> map);
    public CommunityPostDto findById(long postId);
    public int updateHitCount(long postId) throws Exception;

    // 이전글·다음글
    public CommunityPostDto findByPrev(Map<String, Object> map);
    public CommunityPostDto findByNext(Map<String, Object> map);

    // 좋아요 조회·등록·취소
    public int hasPostLike(Map<String, Object> map);
    public int postLikeCount(long postId);
    public int insertPostLike(Map<String, Object> map) throws Exception;
    public int deletePostLike(Map<String, Object> map) throws Exception;
}