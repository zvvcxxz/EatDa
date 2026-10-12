package com.sp.app.admin.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.admin.domain.dto.AdminNoticeDto;

@Mapper
public interface AdminNoticeMapper {
	public void insertNotice(AdminNoticeDto dto) throws SQLException;
	public void updateNotice(AdminNoticeDto dto) throws SQLException;
	public void deleteNotice(long noticeId) throws SQLException;
	
	public int dataCount(Map<String, Object> map);
	public List<AdminNoticeDto> listNoticeTop();
	public List<AdminNoticeDto> listNotice(Map<String, Object> map);
	
	public AdminNoticeDto findById(long noticeId);
	public void updateHitCount(long noticeId) throws SQLException;
	public AdminNoticeDto findByPrev(Map<String, Object> map);
	public AdminNoticeDto findByNext(Map<String, Object> map);
	
	public void insertNoticeFile(AdminNoticeDto dto) throws SQLException;
	public List<AdminNoticeDto> listNoticeFile(long noticeId);
	public AdminNoticeDto findByFileId(long noticeFileId);
	public void deleteNoticeFile(Map<String, Object> map) throws SQLException;
}
