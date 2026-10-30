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
	private final CustomLogoutHandler customLogoutHandler;
	
	@Bean
	SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

		HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
		requestCache.setMatchingRequestParameterName(null);
		requestCache.setRequestMatcher(request -> {
            String uri = request.getRequestURI();
            String accept = request.getHeader("Accept");
            String xmlHeader = request.getHeader("X-Requested-With");

            // /api/ 경로, SSE 요청, AJAX 요청은 RequestCache에 저장하지 않음
            if (uri.startsWith("/api/") || 
                uri.startsWith("/ws-chat/") || 
                "XMLHttpRequest".equals(xmlHeader) || 
                (accept != null && accept.contains("text/event-stream"))) {
                return false;
            }
            return true;
        });

		String[] excludeUri = { "/", "/index.html", "/member/login", "/member/account", "/member/logout",
				"/member/userIdCheck", "/member/complete", "/member/pwdFind", "/member/expired",
				"/dist/**", "/error/**", "/ws-chat/**", "/member/idFind",
				"/guest/main", "/guest/list", "/uploads/photo/**", "/favicon.ico", 
				"/oauth/kakao/callback" ,"/css/**", "/js/**", "/images/**", "/home/**",
				"/uploads/community/recipes/**","/uploads/community/cooking/**", "/home/recipe-slides",
				"/api/notifications/subscribe"
				};

		http.cors(Customizer.withDefaults())
			.csrf(AbstractHttpConfigurer::disable)
			.requestCache(request -> request.requestCache(requestCache));

		http.authorizeHttpRequests(authorize -> authorize
			.requestMatchers(excludeUri).permitAll()
			.requestMatchers("/admin/**").hasAnyRole("ADMIN")
			.requestMatchers("/**").hasAnyRole("USER", "ADMIN")
			.anyRequest().authenticated()
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
			.addLogoutHandler(customLogoutHandler) //로그아웃 시 대화방 정리
			.invalidateHttpSession(true)
			.deleteCookies("JSESSIONID")
			.logoutSuccessUrl("/")
		)
		.addFilterAfter(ajaxSessionTimeoutFilter(), ExceptionTranslationFilter.class)
		.sessionManagement(management -> management
			.maximumSessions(1)
			.expiredUrl("/member/expired"));

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