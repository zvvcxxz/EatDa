package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;

import com.sp.app.admin.domain.dto.AdminMemberDto;

public interface AdminMemberService {
	public int dataCount(Map<String, Object> map);
	public List<AdminMemberDto> listMember(Map<String, Object> map);
	
	public AdminMemberDto findById(Long memberId);
	
	public void updateMember(Map<String, Object> map) throws Exception;
	public void updateMemberStatus(AdminMemberDto dto) throws Exception;
	
	public List<AdminMemberDto> listMemberStatusHist(Long memberId);
}
