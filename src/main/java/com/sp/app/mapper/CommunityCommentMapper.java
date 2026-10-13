package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.CommunityCommentDto;

@Mapper
public interface CommunityCommentMapper {
    // 댓글과 답글 등록
	public void insertComment(CommunityCommentDto dto) throws SQLException;

    // 댓글 개수와 페이징 목록
	public int commentCount(Map<String, Object> map);
	public List<CommunityCommentDto> listComment(Map<String, Object> map);

    // 답글 목록과 개수
	public List<CommunityCommentDto> listCommentAnswer(Map<String, Object> map);
	public int commentAnswerCount(Map<String, Object> map);

    // 작성자와 상태 확인
	public CommunityCommentDto findById(long commentId);

	public int updateComment(CommunityCommentDto dto) throws SQLException;
	public int deleteComment(Map<String, Object> map) throws SQLException;
	public int hideComment(Map<String, Object> map) throws SQLException;
}