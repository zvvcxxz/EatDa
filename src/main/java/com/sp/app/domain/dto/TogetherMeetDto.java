package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// 커뮤니티 - 같이먹어요
public class TogetherMeetDto {
	private Long meet_id;		// 같이먹어요 게시글 번호(PK)
	private Long author_id;		// 작성자 회원 식별자 (PK)
	
	private String name; 		// 작성자 이름 - 회원테이블과 조인해서 가져와야함
	private String title;		// 모임 제목
	private String content;		// 모임 소개 / 안내 내용
	private Integer status;			// 게시글 상태
	private Long view_count;		// 조회 수
	
	private String location_text;// 식사 모임 장소
	private String meet_at;		// 식사 모임 일시
	private String application_deadline;// 신청 마감 일시
	
	private Integer meet_status;	// 모임 진행 상태
	private Integer max_participants;// 최대 참여 가능 인원
	
	
	private String created_at;	// 작성 일시
	private String updated_at;	// 수정 일시
	private String deleted_at;	// 삭제 처리 일시
}
