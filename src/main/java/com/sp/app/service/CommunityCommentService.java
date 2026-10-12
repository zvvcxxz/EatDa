package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityCommentDto;

public interface CommunityCommentService {
    void insertComment(CommunityCommentDto dto) throws Exception;

    int commentCount(Map<String, Object> map);
    List<CommunityCommentDto> listComment(Map<String, Object> map);

    List<CommunityCommentDto> listCommentAnswer(Map<String, Object> map);
    int commentAnswerCount(Map<String, Object> map);

    CommunityCommentDto findById(long commentId);

    int updateComment(CommunityCommentDto dto) throws Exception;
    int deleteComment(Map<String, Object> map) throws Exception;
    int hideComment(Map<String, Object> map) throws Exception;
}