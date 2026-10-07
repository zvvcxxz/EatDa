package com.sp.app.domain.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberDto {
		// 기본 회원 식별 및 인증 정보
		private long member_id;
		private long role_id;     
		private String login_id;
		private String password;       // PASSWORD_HASH
		private String name;
		private String email;
		private String tel;            // PHONE 
		private String profile_photo;  // PROFILE_URL 
		
		// 계정 상태 및 로그인 보안
		private int enabled;  // STATUS 매핑 (1: 정상, 2: 정지, 3: 탈퇴)
		private int failure_cnt;
		private String last_login;     // LAST_LOGIN_AT
		
		// 인적사항 및 수신동의
		private String birth;
		private String zip;
		private String addr1;
		private String addr2;
		private int receive_email;
		
		// 소셜 로그인 관련
		private String sns_provider;
		private String sns_id;
		
		// 생성 및 수정 시간
		private String created_at;
		private String update_at;
		
		// 권한 정보 (Spring Security / APP_ROLE 조인용)
		private String authority; // ROLE_CODE('ROLE_USER', 'ROLE_ADMIN')
		private String role_name; // ROLE_NAME('일반회원', '관리자')
		
		// 파일 업로드 처리용
		private MultipartFile selectFile;	
}
