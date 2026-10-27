package com.sp.app.admin.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import com.sp.app.admin.domain.dto.AdminInquiryDto;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;

@Mapper
public interface AdminInquiryMapper {
    public int dataCount(Map<String, Object> map);
    public List<AdminInquiryDto> listInquiry(Map<String, Object> map);
    public AdminInquiryDto findById(long inquiryId);
    public List<AdminInquiryDto> listFollowUp(long parentId);
    public List<InquiryFileDto> listInquiryFile(long inquiryId);
    public void updateInquiryAnswer(AdminInquiryDto dto);
}