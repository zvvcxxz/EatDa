package com.sp.app.domain.dto;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class MyInquiryDto {

    // 문의 게시글 정보
    private Long inquiryId;
    private Long memberId;
    private Integer inquiryType;
    //SERVICE 0, RECIPE 1, COMMUNITY 2, OTHER 3
    private String title;
    private String content;
    private Integer status;
    //처리 상태 RECEIVED문의중:0, ANSWERED답변완료:1
    private Long parentInquiryId; //추가문의시 원본글

    // 관리자 답변 정보
    private Long answerAdminId;
    private String answerContent;
    private LocalDateTime answeredAt;

    // 등록 및 수정 일시
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 사용자가 업로드 요청으로 보낸 파일
    private List<MultipartFile> uploadFiles;

    // DB에서 조회한 첨부파일 메타데이터
    private List<InquiryFileDto> files;

    @Getter
    @Setter
    @NoArgsConstructor
    @ToString
    public static class InquiryFileDto {//첨부파일
        private Long inquiryFileId;
        private Long inquiryId;
        private String originalFileName;
        private String filePath;// 시스템 저장 파일명
    }
}
