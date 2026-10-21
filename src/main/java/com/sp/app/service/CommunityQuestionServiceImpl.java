package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.CommunityQuestionDto;
import com.sp.app.mapper.CommunityQuestionMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityQuestionServiceImpl implements CommunityQuestionService {
	private final CommunityQuestionMapper mapper;
	
	@Override
	public void insertQuestion(CommunityQuestionDto dto) throws Exception {
		try {
			mapper.insertQuestion(dto);
		} catch (Exception e) {
			log.info("insertQuestion : ", e);
			throw e;
		}
		
	}

	@Override
	public int updateQuestion(CommunityQuestionDto dto) throws Exception {
		try {
			return mapper.updateQuestion(dto);
		} catch (Exception e) {
			log.info("updateQuestion : ", e);
			throw e;
		}
	}

	@Override
	public int deleteQuestion(Map<String, Object> map) throws Exception {
		try {
			// 실제 행은 남기고 삭제 상태로 변경
			return mapper.deleteQuestion(map);
		} catch (Exception e) {
			log.info("deleteQuestion : ", e);
			throw e;			
		}
	}

	@Override
	public int hideQuestion(long postId) throws Exception {
		try {
            return mapper.hideQuestion(postId);
        } catch (Exception e) {
            log.info("hideQuestion : ", e);
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
	public List<CommunityQuestionDto> listQuestion(Map<String, Object> map) {
		List<CommunityQuestionDto> list = List.of();

        try {
            list = mapper.listQuestion(map);
        } catch (Exception e) {
            log.info("listQuestion : ", e);
        }

        return list;
	}

	@Override
	public CommunityQuestionDto findById(long postId) {
		CommunityQuestionDto dto = null;

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
	public CommunityQuestionDto findByPrev(Map<String, Object> map) {
		CommunityQuestionDto dto = null;

        try {
            dto = mapper.findByPrev(map);
        } catch (Exception e) {
            log.info("findByPrev : ", e);
        }

        return dto;
	}

	@Override
	public CommunityQuestionDto findByNext(Map<String, Object> map) {
		CommunityQuestionDto dto = null;

	        try {
	            dto = mapper.findByNext(map);
	        } catch (Exception e) {
	            log.info("findByNext : ", e);
	        }

	        return dto;
	}
}
