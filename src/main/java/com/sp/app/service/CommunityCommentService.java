package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityCommentDto;

public interface CommunityCommentService {
    public void insertComment(CommunityCommentDto dto) throws Exception;

    public int commentCount(Map<String, Object> map);
    public List<CommunityCommentDto> listComment(Map<String, Object> map);

    public List<CommunityCommentDto> listCommentAnswer(Map<String, Object> map);
    public int commentAnswerCount(Map<String, Object> map);

    public CommunityCommentDto findById(long commentId);

    public int updateComment(CommunityCommentDto dto) throws Exception;
    public int deleteComment(Map<String, Object> map) throws Exception;
    public int hideComment(Map<String, Object> map) throws Exception;
}