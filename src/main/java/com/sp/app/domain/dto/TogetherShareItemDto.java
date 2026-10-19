package com.sp.app.domain.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TogetherShareItemDto {
	private Long share_item_id;		// 나눔 재료 번호
	private Long share_id;			// 재료 나눔 게시글 번호
	private Long ingredient_id;		// 재료 번호
	
	private String ingredient_name;	// 재료 이름
	private BigDecimal quantity;	// 수량 - 소수점 가능
	private String unit;			// 단위
	private String expires_on;		// 소비기한
	private String item_note; // 나눔 재료 설명
}
