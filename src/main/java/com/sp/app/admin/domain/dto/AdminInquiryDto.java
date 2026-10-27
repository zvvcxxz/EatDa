package com.sp.app.admin.domain.dto;

import java.util.List;
import com.sp.app.domain.dto.MyInquiryDto.InquiryFileDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminInquiryDto {
    private Long inquiryId;
    private Long memberId;
    private String userName;
    private String loginId;
    
    private Integer inquiryType;
    private String title;
    private String content;
    private Integer status;
    private Long parentInquiryId;
    
    private Long answerAdminId;
    private String answerAdminName;
    private String answerContent;
    private String answeredAt;
    
    private String createdAt;
    
    private List<InquiryFileDto> files;
    private List<AdminInquiryDto> followUps;
}