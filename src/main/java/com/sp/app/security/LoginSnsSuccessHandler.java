package com.sp.app.security;

import java.util.Arrays;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.sp.app.common.RequestUtils;
import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.SessionInfo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoginSnsSuccessHandler {
	public void forceLogin(MemberDto dto) throws Exception{
		try {
			String authCode = (dto.getAuthority() != null) ? dto.getAuthority() : "ROLE_USER";
	        long roleId = (dto.getRole_id() != 0) ? dto.getRole_id() : 2L;
	        String loginId = (dto.getLogin_id() != null) ? dto.getLogin_id() : dto.getSns_provider() + "_" + dto.getSns_id();

	        SessionInfo info = SessionInfo.builder()
	                .member_id(dto.getMember_id())
	                .role_id(roleId)
	                .login_id(loginId)
	                .name(dto.getName())
	                .email(dto.getEmail())
	                .authority(authCode)
	                .userLevel(NumericRoleGranted.getUserLevel(authCode))
	                .avatar(dto.getProfile_photo())
	                .login_type(dto.getSns_provider())
	                .build();
			
			CustomUserDetails userDetails = CustomUserDetails.builder()
					.sessionInfo(info)
					.disabled(false)
					.roles(Arrays.asList("USER"))
					.build();
			
			// 시큐리티 로그인 처리
			Authentication authentication =
					new UsernamePasswordAuthenticationToken(
							userDetails,
							null,	// sns 로그인은 패스워드를 null 로 설정
							userDetails.getAuthorities()
					);

			SecurityContext securityContext = SecurityContextHolder.getContext();
			securityContext.setAuthentication(authentication);
			
			// HttpSession에 SecurityContext를 직접 저장
			// "SPRING_SECURITY_CONTEXT"는 시큐리티가 세션에서 인증 정보를 찾을 때 사용하는 표준 키
			HttpServletRequest request = RequestUtils.getCurrentRequest();
			HttpSession session = request.getSession(true);
			session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);	
		    
		} catch (Exception e) {
			log.info("forceLogin : ", e);
			
			throw e;
		}
	}
}
