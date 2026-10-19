package com.sp.app.admin.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminTagDto {
	private long tagId;
	private String tagName;
	private String tagGroup;
	private String activeYn;
	private String createdAt;
	private String updateAt;
}
