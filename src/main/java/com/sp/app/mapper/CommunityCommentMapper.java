package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.CommunityCommentDto;

@Mapper
public interface CommunityCommentMapper {
    // 댓글과 답글 등록
    void insertComment(CommunityCommentDto dto) throws SQLException;

    // 댓글 개수와 페이징 목록
    int commentCount(Map<String, Object> map);
    List<CommunityCommentDto> listComment(Map<String, Object> map);

    // 답글 목록과 개수
    List<CommunityCommentDto> listCommentAnswer(Map<String, Object> map);
    int commentAnswerCount(Map<String, Object> map);

    // 작성자와 상태 확인
    CommunityCommentDto findById(long commentId);

    int updateComment(CommunityCommentDto dto) throws SQLException;
    int deleteComment(Map<String, Object> map) throws SQLException;
    int hideComment(Map<String, Object> map) throws SQLException;
}