package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.CommunityAnswerDto;
import com.sp.app.mapper.CommunityAnswerMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommunityAnswerServiceImpl implements CommunityAnswerService {
	private final CommunityAnswerMapper mapper;
	
	// 등록 실패는 컨트롤러에서 처리하도록 전달
	@Override
	public void insertAnswer(CommunityAnswerDto dto) throws Exception {
		try {
			mapper.insertAnswer(dto);
		} catch (Exception e) {
			log.info("insertAnswer : ", e);
			throw e;
		}
		
	}

	@Override
	public int answerCount(Map<String, Object> map) {
		int result = 0;
		
		try {
			result = mapper.answerCount(map);
		} catch (Exception e) {
			log.info("answerCount : ", e);
		}
		
		return result;
	}

	@Override
	public List<CommunityAnswerDto> listAnswer(Map<String, Object> map) {
		List<CommunityAnswerDto> list = List.of();
		
		try {
			list = mapper.listAnswer(map);
		} catch (Exception e) {
			log.info("listAnswer : ", e);
		}
		
		return list;
	}

	@Override
	public CommunityAnswerDto findById(long commentId) {
		CommunityAnswerDto dto = null;
		
		try {
			dto = mapper.findById(commentId);
		} catch (Exception e) {
			log.info("findById : ", e);
		}
		
		return dto;
	}
	
	// 수정·삭제·숨김 결과를 컨트롤러에 전달
	@Override
	public int updateAnswer(CommunityAnswerDto dto) throws Exception {
		try {
            return mapper.updateAnswer(dto);
        } catch (Exception e) {
            log.info("updateAnswer : ", e);
            throw e;
        }
	}

	@Override
	public int deleteAnswer(Map<String, Object> map) throws Exception {
		try {
            return mapper.deleteAnswer(map);
        } catch (Exception e) {
            log.info("deleteAnswer : ", e);
            throw e;
        }
	}

	@Override
	public int hideAnswer(Map<String, Object> map) throws Exception {
		try {
            return mapper.hideAnswer(map);
        } catch (Exception e) {
            log.info("hideAnswer : ", e);
            throw e;
        }
    }
}
