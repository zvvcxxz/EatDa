package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.MyInquiryDto;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;

public interface MyInquiryService {
	
    List<MyInquiryDto> selectMyInquiryList(Map<String, Object> map);   
    long countMyInquiryList(Map<String, Object> map);

    MyInquiryDto selectMyInquiryDetail(Long inquiryId, Long memberId);

    int insertMyInquiry(MyInquiryDto dto);
    int updateMyInquiry(MyInquiryDto dto);
    int deleteMyInquiry(Long inquiryId, Long memberId);
  
    List<MyInquiryDto> selectFollowUpInquiries(Long parentInquiryId, Long memberId);
    List<InquiryFileDto> selectInquiryFiles(Long inquiryId);   
    InquiryFileDto selectInquiryFile(Long inquiryFileId, Long inquiryId);
   
    int insertInquiryFile(InquiryFileDto dto);
    int deleteInquiryFilesByInquiryId(Long inquiryId);    
    int deleteInquiryFile(Long inquiryFileId, Long inquiryId);
}
