package com.sp.app.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.sp.app.common.RequestUtils;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.oauth.KakaoAuthService;
import com.sp.app.oauth.KakaoUser;
import com.sp.app.security.LoginSnsSuccessHandler;
import com.sp.app.service.MemberService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MemberSnsController {
	private final MemberService memberService;
	private final KakaoAuthService kakaoService;
	private final LoginSnsSuccessHandler successHandler;

	public static final String SNS_PROVIDER_KAKAO = "kakao";
	public static final String SNS_PROVIDER_NAVER = "naver";
	public static final String SNS_PROVIDER_GOOGLE = "google";
	
	@GetMapping("/oauth/kakao/callback")
	public ResponseEntity<?> kakaoLogin(@RequestParam("code") String code) {
		try {
			String accessToken = kakaoService.getAccessToken(code);
			KakaoUser kakaoUser = kakaoService.getUserInfo(accessToken);
			String sns_id = kakaoUser.getId().toString();
			String sns_provider = SNS_PROVIDER_KAKAO;
			
			Map<String, Object> map = new HashMap<>();
			map.put("sns_id", sns_id);
			map.put("sns_provider", sns_provider);
			
			MemberDto dto = memberService.loginSnsMember(map);
			if(dto == null) {
				dto = new MemberDto();
				
				dto.setSns_id(sns_id);
				dto.setSns_provider(sns_provider);
				dto.setName(kakaoUser.getNickname());
				dto.setEmail(kakaoUser.getEmail());
				
				memberService.insertSnsMember(dto);
			}
			
			// 시큐리티 로그인 처리
			successHandler.forceLogin(dto);
			
		} catch (Exception e) {
			log.info("kakaoLogin : ", e);
		}
		
		String cp = RequestUtils.getContextPath();
		String uri = cp + "/";
		
		StringBuilder sb = new StringBuilder();
		sb.append("<script>");
		sb.append("window.opener.location.replace('" + uri + "');");
		sb.append("window.close()"); // 로그인 팝업창 닫기
		sb.append("</script>");
		
		return ResponseEntity.ok()
				.contentType(MediaType.valueOf("text/html; charset=utf-8"))
				.body(sb.toString());
	}
}
