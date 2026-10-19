package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;

import com.sp.app.admin.domain.dto.AdminTagDto;

public interface AdminTagService {
	public void insertTag(AdminTagDto dto) throws Exception;
	public void updateTag(AdminTagDto dto) throws Exception;
	public void deleteTag(long tagId) throws Exception;
	
	public int dataCount(Map<String, Object> map);
	public List<AdminTagDto> listTag(Map<String, Object> map);
	
	public AdminTagDto findById(long tagId);
	public AdminTagDto findByName(String tagName);
}
