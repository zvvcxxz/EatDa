package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityAnswerDto;

public interface CommunityAnswerService {
	public void insertAnswer(CommunityAnswerDto dto) throws Exception;
	
	public int answerCount(Map<String, Object> map);
	public List<CommunityAnswerDto> listAnswer(Map<String, Object> map);
	
	public CommunityAnswerDto findById(long commentId);
	
	public int updateAnswer(CommunityAnswerDto dto) throws Exception;
	public int deleteAnswer(Map<String, Object> map) throws Exception;
	public int hideAnswer(Map<String, Object> map) throws Exception;
}
