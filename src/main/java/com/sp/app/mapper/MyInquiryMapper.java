package com.sp.app.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.dto.MyInquiryDto;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;

@Mapper
public interface MyInquiryMapper {

	// 내 문의 목록: 검색 + 유형 필터 + 페이징
    List<MyInquiryDto> selectMyInquiryList(Map<String, Object> map);

    // 검색 조건에 맞는 전체 문의 개수
    long countMyInquiryList(Map<String, Object> map);

    // 내 문의 상세
    MyInquiryDto selectMyInquiryDetail(
        @Param("inquiryId") Long inquiryId, @Param("memberId") Long memberId);
    
    // 추가 문의 목록
    List<MyInquiryDto> selectFollowUpInquiries(
    		@Param("parentInquiryId") Long parentInquiryId,
    		@Param("memberId") Long memberId);
    
    //추가문의 상세 (부모id 검사)
    MyInquiryDto selectFollowUpDetail(@Param("inquiryId") Long inquiryId,
    		@Param("followUpId") Long followUpId, @Param("memberId") Long memberId);

    int insertMyInquiry(MyInquiryDto dto);// 문의 등록   
    int updateMyInquiry(MyInquiryDto dto);// 문의 수정
    int deleteMyInquiry(@Param("inquiryId") Long inquiryId, 
    		@Param("memberId") Long memberId);// 문의 삭제

    
    int insertInquiryFile(InquiryFileDto dto);// 첨부파일 등록

    // 문의의 첨부파일 목록
    List<InquiryFileDto> selectInquiryFiles(@Param("inquiryId") Long inquiryId);

    // 특정 첨부파일 조회
    InquiryFileDto selectInquiryFile(
        @Param("inquiryFileId") Long inquiryFileId,
        @Param("inquiryId") Long inquiryId);

    // 문의에 연결된 첨부파일 DB 정보 전체 삭제
    int deleteInquiryFilesByInquiryId(@Param("inquiryId") Long inquiryId);

    // 특정 첨부파일 DB 정보 삭제
    int deleteInquiryFile(@Param("inquiryFileId") Long inquiryFileId,
        @Param("inquiryId") Long inquiryId);
    
    //수정, 삭제 가능한 문의인지(status = 0) 인지 확인용
    int selectInquiryByStatus(@Param("inquiryId") Long inquiryId, @Param("memberId") Long memberId);
    
    
}
