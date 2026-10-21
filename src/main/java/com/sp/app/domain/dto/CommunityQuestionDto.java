package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityQuestionDto {
	
	private long postId;
	private long authorId;
	private String authorName;
	private String title;
	private String content;
	private long viewCount;
	private String createdAt;
	
	private int commentCount;
}
