package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.CommunityPostDto;
import com.sp.app.mapper.CommunityPostMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityPostServiceImpl implements CommunityPostService {
    private final CommunityPostMapper mapper;

    @Override
    public void insertPost(CommunityPostDto dto) throws Exception {
        try {
            mapper.insertPost(dto);
        } catch (Exception e) {
            log.info("insertPost : ", e);
            throw e;
        }
    }

    @Override
    public int updatePost(CommunityPostDto dto) throws Exception {
        try {
            return mapper.updatePost(dto);
        } catch (Exception e) {
            log.info("updatePost : ", e);
            throw e;
        }
    }

    @Override
    public int deletePost(Map<String, Object> map) throws Exception {
        try {
            // 실제 행은 남기고 삭제 상태로 변경
            return mapper.deletePost(map);
        } catch (Exception e) {
            log.info("deletePost : ", e);
            throw e;
        }
    }

    @Override
    public int hidePost(long postId) throws Exception {
        try {
            return mapper.hidePost(postId);
        } catch (Exception e) {
            log.info("hidePost : ", e);
            throw e;
        }
    }

    @Override
    public int dataCount(Map<String, Object> map) {
        int result = 0;

        try {
            result = mapper.dataCount(map);
        } catch (Exception e) {
            log.info("dataCount : ", e);
        }

        return result;
    }

    @Override
    public List<CommunityPostDto> listPost(Map<String, Object> map) {
        List<CommunityPostDto> list = List.of();

        try {
            list = mapper.listPost(map);
        } catch (Exception e) {
            log.info("listPost : ", e);
        }

        return list;
    }

    @Override
    public CommunityPostDto findById(long postId) {
        CommunityPostDto dto = null;

        try {
            dto = mapper.findById(postId);
        } catch (Exception e) {
            log.info("findById : ", e);
        }

        return dto;
    }

    @Override
    public int updateHitCount(long postId) throws Exception {
        try {
            return mapper.updateHitCount(postId);
        } catch (Exception e) {
            log.info("updateHitCount : ", e);
            throw e;
        }
    }

    @Override
    public CommunityPostDto findByPrev(Map<String, Object> map) {
        CommunityPostDto dto = null;

        try {
            dto = mapper.findByPrev(map);
        } catch (Exception e) {
            log.info("findByPrev : ", e);
        }

        return dto;
    }

    @Override
    public CommunityPostDto findByNext(Map<String, Object> map) {
        CommunityPostDto dto = null;

        try {
            dto = mapper.findByNext(map);
        } catch (Exception e) {
            log.info("findByNext : ", e);
        }

        return dto;
    }

    @Override
    public int hasPostLike(Map<String, Object> map) {
        int result = 0;

        try {
            result = mapper.hasPostLike(map);
        } catch (Exception e) {
            log.info("hasPostLike : ", e);
        }

        return result;
    }

    @Override
    public int postLikeCount(long postId) {
        int result = 0;

        try {
            result = mapper.postLikeCount(postId);
        } catch (Exception e) {
            log.info("postLikeCount : ", e);
        }

        return result;
    }

    @Override
    public int insertPostLike(Map<String, Object> map) throws Exception {
        try {
            return mapper.insertPostLike(map);
        } catch (Exception e) {
            log.info("insertPostLike : ", e);
            throw e;
        }
    }

    @Override
    public int deletePostLike(Map<String, Object> map) throws Exception {
        try {
            return mapper.deletePostLike(map);
        } catch (Exception e) {
            log.info("deletePostLike : ", e);
            throw e;
        }
    }
}