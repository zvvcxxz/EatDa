package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityQuestionDto;

public interface CommunityQuestionService {
	
	// 게시글 등록·수정·삭제
	public void insertQuestion(CommunityQuestionDto dto) throws Exception;
	public int updateQuestion(CommunityQuestionDto dto) throws Exception;
	public int deleteQuestion(Map<String, Object> map) throws Exception;
	public int hideQuestion(long postId) throws Exception;
	
	// 목록과 상세 조회
	public int dataCount(Map<String, Object> map);
	public List<CommunityQuestionDto> listQuestion(Map<String, Object> map);
	public CommunityQuestionDto findById(long postId);
	public int updateHitCount(long postId) throws Exception;
	
	// 이전글·다음글
	public CommunityQuestionDto findByPrev(Map<String, Object> map);
	public CommunityQuestionDto findByNext(Map<String, Object> map);
}
