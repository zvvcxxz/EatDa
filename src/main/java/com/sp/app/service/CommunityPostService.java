package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityPostDto;

// 게시글 기능 선언
public interface CommunityPostService {
	
	// 게시글 등록
	public void insertPost(CommunityPostDto dto);
	
	// 검색 조건에 맞는 글 수와 목록
    public int dataCount(Map<String, Object> map);

    public List<CommunityPostDto> listPost(Map<String, Object> map);
    
    // 상세 조회와 조회수 증가
    public CommunityPostDto findById(long postId);

    public int updateHitCount(long postId);
    
    // 게시글 수정·삭제·숨김
    public int updatePost(CommunityPostDto dto);

    public int deletePost(Map<String, Object> map);

    public int hidePost(long postId);
    
    // 이전글·다음글
    public CommunityPostDto findByPrev(Map<String, Object> map);

    public CommunityPostDto findByNext(Map<String, Object> map);
    
    // 내가 누른 좋아요 확인과 전체 좋아요 수
    public int hasPostLike(Map<String, Object> map);
    
    public int postLikeCount(long postId);
    
    // 좋아요 등록·취소
    public int insertPostLike(Map<String, Object> map);
    
    public int deletePostLike(Map<String, Object> map);
}
