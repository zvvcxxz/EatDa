package com.sp.app.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 세션에 저장할 정보(아이디, 이름, 역할(권한) 등)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SessionInfo {
	
	private long member_id;      // 회원 고유 PK
	private long role_id;        // 권한 ID (1: ADMIN, 2: USER)
    private String login_id;     // 로그인 아이디
    private String name;         // 회원 이름 (또는 nickname)
    private String email;        // 이메일
    private String authority;    // 권한 (예: "ROLE_USER", "ROLE_ADMIN")
    private int userLevel;
    private String login_type;   // local, kakao, naver, google 등
    private String avatar; // profile photo
    private String password;
}
