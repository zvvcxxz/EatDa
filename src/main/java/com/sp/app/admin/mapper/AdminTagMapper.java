package com.sp.app.admin.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.admin.domain.dto.AdminTagDto;

@Mapper
public interface AdminTagMapper {
	public void insertTag(AdminTagDto dto) throws SQLException;
	public void updateTag(AdminTagDto dto) throws SQLException;
	public void deleteTag(long tagId) throws SQLException;
	
	public int dataCount(Map<String, Object> map);
	public List<AdminTagDto> listTag(Map<String, Object> map);
	
	public AdminTagDto findById(long tagId);
	public AdminTagDto findByName(String tagName);
}
