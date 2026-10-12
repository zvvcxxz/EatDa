package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TogetherMeetParticipantDto {
	private Long meet_id;
	private Long member_id;
	private String name;
	private Integer status;
	private String created_at;
}
