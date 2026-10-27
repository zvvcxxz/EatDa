package com.sp.app.admin.service;

import java.util.List;
import java.util.Map;
import com.sp.app.admin.domain.dto.AdminInquiryDto;

public interface AdminInquiryService {
    public int dataCount(Map<String, Object> map);
    public List<AdminInquiryDto> listInquiry(Map<String, Object> map);
    public AdminInquiryDto findById(long inquiryId);
    public void updateInquiryAnswer(AdminInquiryDto dto) throws Exception;
}