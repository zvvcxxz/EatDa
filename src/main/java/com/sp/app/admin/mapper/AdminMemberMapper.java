package com.sp.app.admin.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.admin.domain.dto.AdminMemberDto;

@Mapper
public interface AdminMemberMapper {
	public int dataCount(Map<String, Object> map);
	public List<AdminMemberDto> listMember(Map<String, Object> map);
	
	public AdminMemberDto findById(Long memberId);
	
	public void updateMember(Map<String, Object> map) throws SQLException;
	public void updateMemberStatus(Map<String, Object> map) throws SQLException;
	public void updateFailureCountReset(Long memberId) throws SQLException;
	
	public void insertMemberStatusHist(AdminMemberDto dto) throws SQLException;
	public List<AdminMemberDto> listMemberStatusHist(Long memberId);
	
	public List<Map<String, Object>> listAgeSection();
}
