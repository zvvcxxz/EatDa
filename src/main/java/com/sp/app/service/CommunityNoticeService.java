package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityNoticeDto;

public interface CommunityNoticeService {
	public int dataCount(Map<String, Object> map);
	public List<CommunityNoticeDto> listNotice(Map<String, Object> map);
	
	public void updateHitCount(long noticeId) throws Exception;
	public CommunityNoticeDto findById(long noticeId);
	
	public CommunityNoticeDto findByPrev(Map<String, Object> map);
	public CommunityNoticeDto findByNext(Map<String, Object> map);
	
	public List<CommunityNoticeDto> listNoticeFile(long noticeId);
	public CommunityNoticeDto findByFileId(long fileId);
}
