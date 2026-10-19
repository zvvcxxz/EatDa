package com.sp.app.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.dto.MyInquiryDto;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;

@Mapper
public interface MyInquiryMapper {

    // 내 문의 목록 조회
    List<MyInquiryDto> selectMyInquiryList(@Param("memberId") Long memberId);

    // 내 문의 상세 조회
    MyInquiryDto selectMyInquiryDetail(@Param("inquiryId") Long inquiryId, 
    		@Param("memberId") Long memberId);

    // 문의 게시글 등록
    int insertMyInquiry(MyInquiryDto dto);

    // 미답변 문의 수정
    int updateMyInquiry(MyInquiryDto dto);

    // 미답변 문의 삭제
    int deleteMyInquiry(@Param("inquiryId") Long inquiryId,
        @Param("memberId") Long memberId);
    
    // 특정 문의를 기준으로 등록된 추가 질문 목록 조회
    List<MyInquiryDto> selectFollowUpInquiries(
        @Param("parentInquiryId") Long parentInquiryId,
        @Param("memberId") Long memberId);

    //문의 첨부파일
    // 첨부파일 메타데이터 등록
    int insertInquiryFile(InquiryFileDto dto);

    // 문의별 첨부파일 목록 조회
    List<InquiryFileDto> selectInquiryFiles(@Param("inquiryId") Long inquiryId);

    // 특정 문의에 속한 파일 한 건 조회
    InquiryFileDto selectInquiryFile(@Param("inquiryFileId") Long inquiryFileId,
        @Param("inquiryId") Long inquiryId);

    // 문의의 첨부파일 메타데이터 전체 삭제
    int deleteInquiryFilesByInquiryId(@Param("inquiryId") Long inquiryId);

    // 문의에 속한 첨부파일 한 건의 메타데이터 삭제
    int deleteInquiryFile(@Param("inquiryFileId") Long inquiryFileId,
        @Param("inquiryId") Long inquiryId);
}
