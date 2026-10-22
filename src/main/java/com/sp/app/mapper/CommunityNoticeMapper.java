package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.CommunityNoticeDto;

@Mapper
public interface CommunityNoticeMapper {
	public int dataCount(Map<String, Object> map);
	public List<CommunityNoticeDto> listNotice(Map<String, Object> map);
	
	public void updateHitCount(long noticeId) throws SQLException;
	public CommunityNoticeDto findById(long noticeId);
	
	public CommunityNoticeDto findByPrev(Map<String, Object> map);
	public CommunityNoticeDto findByNext(Map<String, Object> map);
	
	public List<CommunityNoticeDto> listNoticeFile(long noticeId);
	public CommunityNoticeDto findByFileId(long fileId);
}
