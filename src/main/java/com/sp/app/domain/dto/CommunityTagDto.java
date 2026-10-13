package com.sp.app.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommunityTagDto {
    private long tagId;        // 태그 번호
    private String tagName;    // 화면에 표시할 태그명
    private String tagGroup;   // 태그 분류

    private String activeYn;   // Y: 사용, N: 미사용
    private String createdAt;
    private String updatedAt;
}