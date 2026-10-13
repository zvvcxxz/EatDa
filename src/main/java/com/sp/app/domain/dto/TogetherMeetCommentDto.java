package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// 커뮤니티 - 같이먹어요 댓글
public class TogetherMeetCommentDto {
	private Long comment_id;	// 댓글 번호
	private Long meet_id;		// 게시글 번호
	private Long member_id;		// 댓글 작성자 회원 번호
	
	private String name;		// 회원 테이블에서 가져올 작성자 이름
	private String content;		// 댓글 내용
	
	private String created_at;	// 댓글 작성일
	private String updated_at;	// 댓글 수정일
	private String deleted_at;	// 댓글 삭제일
}
