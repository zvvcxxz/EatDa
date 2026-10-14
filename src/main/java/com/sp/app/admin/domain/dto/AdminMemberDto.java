package com.sp.app.admin.domain.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AdminMemberDto {
	private Long memberId;
	private String loginId;
	private String email;
	private String phone;
	private String profileUrl;
	private Integer status;
	private String createdAt;
	private String updateAt;
	private Long roleId;
	private Integer failureCnt;
	private String lastLoginAt;
	private String name;
	private String birth;
	private Integer receiveEmail;
	private String zip;
	private String addr1;
	private String addr2;
	private String snsProvider;
	private String snsId;
	
	private String roleName;
	
	private Long statusHistId;
	private Long adminId;
	private String adminName;
	private String beforeStatus;
	private String afterStatus;
	private String reason;
	private String startAt;
	private String endAt;
}
