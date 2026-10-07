package com.sp.app.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import com.sp.app.domain.dto.MemberDto;
import com.sp.app.service.MemberService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/*
  ## spring security 의 예외
    - BadCredentialException : 비밀번호가 일치하지 않을 때 던지는 예외
    - InternalAuthenticationServiceException : 존재하지 않는 아이디일 때 던지는 예외
    - AuthenticationCredentialNotFoundException : 인증 요구가 거부됐을 때 던지는 예외
    - LockedException : 인증 거부 - 잠긴 계정
    - DisabledException : 인증 거부 - 계정 비활성화(enabled=0)
    - AccountExpiredException : 인증 거부 - 계정 유효기간 만료
    - CredentialExpiredException : 인증 거부 - 비밀번호 유효기간 만료
*/
@Slf4j
public class LoginFailureHandler implements AuthenticationFailureHandler {
	private final MemberService memberService;

	private String defaultFailureUrl;
	
	public LoginFailureHandler(MemberService memberService) {
		this.memberService = memberService;
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {

		String login_id = request.getParameter("login_id");

		String errorMsg = "아이디 또는 패스워드가 일치하지 않습니다.";
		try {
			if (exception instanceof BadCredentialsException) {
				// 패스워드가 일치하지 않을 때 던지는 예외
				
				int cnt = memberService.checkFailureCount(login_id);
				if(cnt <= 4) {
					memberService.updateFailureCount(login_id);
				}
				
				if(cnt >= 4) {
					MemberDto member = memberService.findById(login_id);
	                
	                if (member != null) {
	                    Map<String, Object> map = new HashMap<>();
	                    map.put("status", 2); // STATUS 2: 계정 잠금
	                    map.put("login_id", login_id);
	                    
	                    memberService.updateMemberEnabled(map);
	                }
	                
	                errorMsg = "패스워드를 5회 이상 틀려 계정이 잠겼습니다. 관리자에게 문의하세요.";
	            } else {
	                errorMsg = "아이디 또는 패스워드가 일치하지 않습니다. (로그인 실패 횟수: " + cnt + "회)";
	            }
			} else if (exception instanceof InternalAuthenticationServiceException) {
				
				// 존재하지 않는 아이디 일때 던지는 예외
				errorMsg = "아이디 또는 패스워드가 일치하지 않습니다.";
			} else if (exception instanceof DisabledException) {
				
				// 인증 거부 - 계정 비활성화(enabled=0)
				errorMsg = "계정이 비활성화되었습니다.";
			}
		} catch (Exception e) {
			log.info("LoginFailureHandler : " + errorMsg, e);
		}

		/*
		// 포워딩하면 로그인 실패 후, 다시 로그인을 성공하면 /error 로 이동
		request.setAttribute("message", errorMsg);
		request.getRequestDispatcher(defaultFailureUrl).forward(request, response);
		*/
		
		response.sendRedirect(defaultFailureUrl);
	}

	public void setDefaultFailureUrl(String defaultFailureUrl) {
		this.defaultFailureUrl = defaultFailureUrl;
	}
}
