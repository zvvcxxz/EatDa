package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.CommunityNoticeDto;
import com.sp.app.mapper.CommunityNoticeMapper;

@Service
public class CommunityNoticeServiceImpl implements CommunityNoticeService{
	
	@Autowired
	private CommunityNoticeMapper mapper;
	
	@Override
	public int dataCount(Map<String, Object> map) {
		return mapper.dataCount(map);
	}

	@Override
	public List<CommunityNoticeDto> listNotice(Map<String, Object> map) {
		return mapper.listNotice(map);
	}

	@Override
	public void updateHitCount(long noticeId) throws Exception {
		try {
			mapper.updateHitCount(noticeId);
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public CommunityNoticeDto findById(long noticeId) {
		return mapper.findById(noticeId);
	}

	@Override
	public CommunityNoticeDto findByPrev(Map<String, Object> map) {
		return mapper.findByPrev(map);
	}

	@Override
	public CommunityNoticeDto findByNext(Map<String, Object> map) {
		return mapper.findByNext(map);
	}

	@Override
	public List<CommunityNoticeDto> listNoticeFile(long noticeId) {
		return mapper.listNoticeFile(noticeId);
	}

	@Override
	public CommunityNoticeDto findByFileId(long fileId) {
		return mapper.findByFileId(fileId);
	}
}