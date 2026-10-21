package com.sp.app.service;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.sp.app.domain.dto.MyInquiryDto;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;

public interface MyInquiryService {
	int selectInquiryByStatus(Long inquiryId, Long memberId);
    
    List<MyInquiryDto> selectMyInquiryList(Map<String, Object> map);   
    long countMyInquiryList(Map<String, Object> map);

    MyInquiryDto selectMyInquiryDetail(Long inquiryId, Long memberId);

    int insertMyInquiry(MyInquiryDto dto);
    int updateMyInquiry(MyInquiryDto dto);
    int deleteMyInquiry(Long inquiryId, Long memberId);
  
    List<MyInquiryDto> selectFollowUpInquiries(Long parentInquiryId, Long memberId);
    MyInquiryDto selectFollowUpDetail(Long inquiryId, Long followUpId, Long memberId);
    List<InquiryFileDto> selectInquiryFiles(Long inquiryId);   
    InquiryFileDto selectInquiryFile(Long inquiryFileId, Long inquiryId);
    ResponseEntity<?> fileDownload(Long inquiryId, Long fildId);   
    
    int insertInquiryFile(MultipartFile mf, InquiryFileDto dto);
    int deleteInquiryFilesByInquiryId(Long inquiryId);    
    int deleteInquiryFile(Long inquiryFileId, Long inquiryId);
	boolean deleteUploadFile(String filename);
}
