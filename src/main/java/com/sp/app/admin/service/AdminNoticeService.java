package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;

import com.sp.app.admin.domain.dto.AdminNoticeDto;

public interface AdminNoticeService {
	public void insertNotice(AdminNoticeDto dto, String pathname) throws Exception;
	public void updateNotice(AdminNoticeDto dto, String pathname) throws Exception;
	public void deleteNotice(long noticeId, String pathname) throws Exception;
	
	public int dataCount(Map<String, Object> map);
	public List<AdminNoticeDto> listNoticeTop();
	public List<AdminNoticeDto> listNotice(Map<String, Object> map);
	
	public AdminNoticeDto findById(long noticeId);
	public void updateHitCount(long noticeId) throws Exception;
	public AdminNoticeDto findByPrev(Map<String, Object> map);
	public AdminNoticeDto findByNext(Map<String, Object> map);
	
	public List<AdminNoticeDto> listNoticeFile(long noticeId);
	public AdminNoticeDto findByFileId(long noticeFileId);
	public boolean deleteNoticeFile(long noticeFileId, String pathname) throws Exception;
}
