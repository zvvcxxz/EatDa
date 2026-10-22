package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityNoticeDto {
	private long noticeId;
	private String adminName;
	private String title;
	private String content;
	private String pinnedYn;
	private int hitCount;
	private String createdAt;
	
	private long noticeFileId;
	private String originalFileName;
	private String filePath;
}
