package com.sp.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;

import com.sp.app.service.MemberService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SpringSecurityConfig {
	private final MemberService memberService;
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		// configure HTTP security

		// ?continue 제거를 위해
		// HttpSessionRequestCache
		//  : 인증되지 않는 사용자가 접근하려던 요청을 세션에 저장헤 두었다가, 
		//    로그인 성공 후 다시 원래 요청으로 다시 보내기 위해 사용		
		HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
		requestCache.setMatchingRequestParameterName(null);

		String[] excludeUri = { "/", "/index.html", "/member/login", "/member/account", "/member/logout",
				"/member/userIdCheck", "/member/complete", "/member/pwdFind", "/member/expired",
				"/dist/**", "/error/**", "/ws-chat/**",  
				"/guest/main", "/guest/list", "/uploads/photo/**", "/favicon.ico", 
				"/oauth/kakao/callback" ,"/css/**", "/js/**", "/images/**", "/home/**",
				"/uploads/community/recipes/**","/uploads/community/cooking/**", "/home/recipe-slides"
				};

		http.cors(Customizer.withDefaults()) // CORS 설정 : 기본값 사용
			.csrf(AbstractHttpConfigurer::disable) // CSRF 비활성화
			.requestCache(request -> request.requestCache(requestCache)); // 요청 캐시 설정, ?continue 제거

		http.authorizeHttpRequests(authorize -> authorize
			.requestMatchers(excludeUri).permitAll()
			.requestMatchers("/admin/**").hasAnyRole("ADMIN")
			.requestMatchers("/**").hasAnyRole("USER", "ADMIN") // configurer 에서 ROLE_ 붙여줌
			.anyRequest().authenticated() // 설정 외 모든 요청은 권한과 무관하고 로그인 유저만 사용
		)
		.formLogin(login -> login
			.loginPage("/member/login")
			.loginProcessingUrl("/member/login")
			.usernameParameter("login_id")
			.passwordParameter("password")
			.successHandler(loginSuccessHandler())
			.failureHandler(loginFailureHandler())
			.permitAll()
		)
		.logout(logout -> logout
			.logoutUrl("/member/logout")
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID")
			.logoutSuccessUrl("/")
		)
		.addFilterAfter(ajaxSessionTimeoutFilter(), ExceptionTranslationFilter.class)
		.sessionManagement(management -> management
			.maximumSessions(1)
			// .maxSessionsPreventsLogin(false) // false;기존세션만료(기본), true:신규차단
			.expiredUrl("/member/expired"));

		// 인증 거부 관련 처리
		http.exceptionHandling((exceptionConfig) -> exceptionConfig.accessDeniedPage("/error/noAuthorized"));

		http.headers(headers -> headers
			    .frameOptions(frameOptions -> frameOptions.sameOrigin())
			);
		
		return http.build();
	}

	@Bean
	LoginSuccessHandler loginSuccessHandler() {
		LoginSuccessHandler handler = new LoginSuccessHandler(memberService);
		handler.setDefaultUrl("/");
		return handler;
	}

	@Bean
	LoginFailureHandler loginFailureHandler() {
		LoginFailureHandler handler = new LoginFailureHandler(memberService);
		handler.setDefaultFailureUrl("/member/login?error");
		return handler;
	}

	@Bean
	AjaxSessionTimeoutFilter ajaxSessionTimeoutFilter() {
		AjaxSessionTimeoutFilter filter = new AjaxSessionTimeoutFilter();
		filter.setAjaxHeader("AJAX");
		return filter;
	}
}
