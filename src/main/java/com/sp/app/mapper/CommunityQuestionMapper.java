package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.CommunityQuestionDto;

@Mapper
public interface CommunityQuestionMapper {
	
	// 게시글 등록·수정·삭제
	public void insertQuestion(CommunityQuestionDto dto) throws SQLException;
	public int updateQuestion(CommunityQuestionDto dto) throws SQLException;
	public int deleteQuestion(Map<String, Object> map) throws SQLException;
	public int hideQuestion(long postId) throws SQLException;
	
	// 목록과 상세 조회
	public int dataCount(Map<String, Object> map);
	public List<CommunityQuestionDto> listQuestion(Map<String, Object> map);
	public CommunityQuestionDto findById(long postId);
	public int updateHitCount(long postId) throws SQLException;
	
	// 이전글·다음글
	public CommunityQuestionDto findByPrev(Map<String, Object> map);
	public CommunityQuestionDto findByNext(Map<String, Object> map);
}
