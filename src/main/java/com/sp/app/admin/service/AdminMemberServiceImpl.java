package com.sp.app.admin.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sp.app.admin.domain.dto.AdminMemberDto;
import com.sp.app.admin.mapper.AdminMemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminMemberServiceImpl implements AdminMemberService {

	private final AdminMemberMapper mapper;
	
	@Override
	public int dataCount(Map<String, Object> map) {
		return mapper.dataCount(map);
	}

	@Override
	public List<AdminMemberDto> listMember(Map<String, Object> map) {
		return mapper.listMember(map);
	}

	@Override
	public AdminMemberDto findById(Long memberId) {
		return mapper.findById(memberId);
	}

	@Override
	public void updateMember(Map<String, Object> map) throws Exception {
		try {
			mapper.updateMember(map);
		} catch (Exception e) {
			log.error("updateMember error : ", e);
			throw e;
		}
	}

	@Override
	public void updateMemberStatus(AdminMemberDto dto) throws Exception {
		try {
			Map<String, Object> map = new HashMap<>();
			map.put("memberId", dto.getMemberId());
			map.put("status", dto.getAfterStatus());
			mapper.updateMemberStatus(map);
			
			mapper.insertMemberStatusHist(dto);
			
			if ("1".equals(dto.getAfterStatus())) {
				mapper.updateFailureCountReset(dto.getMemberId());
			}
			
		} catch (Exception e) {
			log.error("updateMemberStatus error : ", e);
			throw e;
		}
	}

	@Override
	public List<AdminMemberDto> listMemberStatusHist(Long memberId) {
		return mapper.listMemberStatusHist(memberId);
	}
}
