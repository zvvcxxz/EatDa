package com.sp.app.admin.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sp.app.admin.mapper.AdminHomeMapper;

@Service
public class AdminHomeServiceImpl implements AdminHomeService {
	
	@Autowired
	private AdminHomeMapper mapper;
	
	@Override
	public Map<String, Object> getDashboardStats() {
		Map<String, Object> map = new HashMap<>();
		
		try {
			// Mapper에서 가져옴
			map.put("memberCount", mapper.memberCount());
			map.put("recipeCount", mapper.recipeCount());
			map.put("pendingReportCount", mapper.pendingReportCount());
			map.put("pendingInquiryCount", mapper.pendingInquiryCount());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
}