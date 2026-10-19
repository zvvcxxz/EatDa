package com.sp.app.domain.dto;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
// 커뮤니티 - 재료 나눔
public class TogetherShareDto {
	private Long share_id;		// 함께 먹어요 게시글 번호
	private Long author_id;		// 재료나눔 게시글 번호
	private String name; 		// 작성자 이름
	
	private String title;		// 게시글 제목
	private String content;		// 게시글 내용
	private Integer status;		// 게시글 상태
	private Long view_count; 	// 조회수
	
	private Integer share_type;		// 나눔 유형: 무료나눔 / 교환
	private String location_text;   // 거래 나눔 장소
	private String available_from;	// 나눔 시작일
	private String available_until; // 나눔 종료일
	private Integer max_recipients;	// 최대 수령, 승인 가능 인원
	private String exchange_note;	// 교환일 때 물품 등 설명
	private Integer share_status;	// 나눔 진행 상태
	
	private String created_at; 		// 글 작성일
	private String updated_at;		// 글 수정일
	private String deleted_at;		// 글 삭제일	
	
	private List<TogetherShareItemDto> items; // 나눔 재료 목록
}
