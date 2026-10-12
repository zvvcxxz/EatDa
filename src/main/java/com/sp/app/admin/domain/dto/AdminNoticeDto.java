package com.sp.app.admin.domain.dto;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminNoticeDto {
    private long noticeId;
    private long adminId;
    private String loginId;
    private String adminName;
    
    private String title;
    private String content;
    
    private String pinnedYn;
    private int status;
    
    private String publishStartAt;
    private String publishEndAt;
    
    private String createdAt;
    private String updatedAt;

    private long noticeFileId;
    private String originalFileName;
    private String filePath;
    
    private int fileCount;
    private List<MultipartFile> selectFile;
    private long gap;
    private int hitCount;
}