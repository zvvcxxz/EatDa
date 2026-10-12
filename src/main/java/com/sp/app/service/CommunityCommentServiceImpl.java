package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.CommunityCommentDto;
import com.sp.app.mapper.CommunityCommentMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityCommentServiceImpl implements CommunityCommentService {
    private final CommunityCommentMapper mapper;

    // 등록 실패는 컨트롤러에서 처리하도록 전달
    @Override
    public void insertComment(CommunityCommentDto dto) throws Exception {
        try {
            mapper.insertComment(dto);
        } catch (Exception e) {
            log.info("insertComment : ", e);
            throw e;
        }
    }

    @Override
    public int commentCount(Map<String, Object> map) {
        int result = 0;

        try {
            result = mapper.commentCount(map);
        } catch (Exception e) {
            log.info("commentCount : ", e);
        }

        return result;
    }

    @Override
    public List<CommunityCommentDto> listComment(Map<String, Object> map) {
        List<CommunityCommentDto> list = List.of();

        try {
            list = mapper.listComment(map);
        } catch (Exception e) {
            log.info("listComment : ", e);
        }

        return list;
    }

    @Override
    public List<CommunityCommentDto> listCommentAnswer(Map<String, Object> map) {
        List<CommunityCommentDto> list = List.of();

        try {
            list = mapper.listCommentAnswer(map);
        } catch (Exception e) {
            log.info("listCommentAnswer : ", e);
        }

        return list;
    }

    @Override
    public int commentAnswerCount(Map<String, Object> map) {
        int result = 0;

        try {
            result = mapper.commentAnswerCount(map);
        } catch (Exception e) {
            log.info("commentAnswerCount : ", e);
        }

        return result;
    }

    @Override
    public CommunityCommentDto findById(long commentId) {
        CommunityCommentDto dto = null;

        try {
            dto = mapper.findById(commentId);
        } catch (Exception e) {
            log.info("findById : ", e);
        }

        return dto;
    }

    // 수정·삭제·숨김 결과를 컨트롤러에 전달
    @Override
    public int updateComment(CommunityCommentDto dto) throws Exception {
        try {
            return mapper.updateComment(dto);
        } catch (Exception e) {
            log.info("updateComment : ", e);
            throw e;
        }
    }

    @Override
    public int deleteComment(Map<String, Object> map) throws Exception {
        try {
            return mapper.deleteComment(map);
        } catch (Exception e) {
            log.info("deleteComment : ", e);
            throw e;
        }
    }

    @Override
    public int hideComment(Map<String, Object> map) throws Exception {
        try {
            return mapper.hideComment(map);
        } catch (Exception e) {
            log.info("hideComment : ", e);
            throw e;
        }
    }
}