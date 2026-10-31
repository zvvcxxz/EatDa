package com.sp.app.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    // SSE 및 비동기 요청 타임아웃 예외 처리 (정상 재연결 과정이므로 에러 페이지를 띄우지 않음)
    @ExceptionHandler(AsyncRequestTimeoutException.class)
    public ModelAndView handleAsyncRequestTimeoutException(HttpServletRequest request, HttpServletResponse response) {
        log.trace("SSE/비동기 요청 타임아웃 발생 (정상 재연결 과정): {}", request.getRequestURI());
        return null; // 뷰를 렌더링하지 않고 요청 종료
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ModelAndView handleMissingParams(MissingServletRequestParameterException ex) {
        ModelAndView mav = new ModelAndView("error/error2");
        
        String title = "잘못된 요청입니다.";
        String errorMessage = "죄송합니다.<br> <strong>400 - 요청을 처리할 수 없습니다.</strong>";
        
        mav.addObject("title", title);
        mav.addObject("message", errorMessage);
        
        log.info("BAD_REQUEST - ", ex);
        
        return mav;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ModelAndView handleArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ModelAndView mav = new ModelAndView("error/error2");
        
        String title = "잘못된 요청입니다.";
        String errorMessage = "죄송합니다.<br> <strong>400 - 요청을 처리할 수 없습니다.</strong>";
        
        mav.addObject("title", title);
        mav.addObject("message", errorMessage);

        log.info("BAD_REQUEST - ", ex);
        
        return mav;
    }
    
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ModelAndView handleResourceNotFound(NoResourceFoundException ex) {
        ModelAndView mav = new ModelAndView("error/error2");
        
        String title = "페이지를 찾을 수 없습니다.";
        String errorMessage = "죄송합니다.<br> <strong>404 - 요청하신 페이지가 존재하지 않습니다.</strong>";
        
        mav.addObject("title", title);
        mav.addObject("message", errorMessage);

        log.info("NOT_FOUND - ", ex);
        
        return mav;
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex) {
        return new ModelAndView("error/uploadFailure");
    }
    
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ModelAndView handleAuthorizationDeniedError(Exception ex) {
        ModelAndView mav = new ModelAndView("error/error2");
        
        String title = "접근 불가.";
        String errorMessage = "죄송합니다.<br> <strong>요청을 처리할 권한이 불충분합니다.</strong>";
        
        mav.addObject("title", title);
        mav.addObject("message", errorMessage);

        log.info("AuthorizationDeniedException 등 - ", ex);
        
        return mav;
    }
    
    //최상위 시스템 예외 처리 (SSE 및 이미 응답이 커밋된 상태에 대한 방어 로직)
    @ExceptionHandler(Exception.class)
    public ModelAndView handleServerError(Exception ex, HttpServletRequest request, HttpServletResponse response) {
        
        // 이미 응답 스트림이 열렸거나 커밋된 상태라면 뷰 렌더링 중단
        if (response.isCommitted()) {
            log.trace("이미 응답이 커밋되어 에러 뷰를 렌더링하지 않습니다: {}", ex.getMessage());
            return null;
        }

        // SSE 요청(text/event-stream) 중에 일반 예외가 발생한 경우 뷰 렌더링 중단
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains(MediaType.TEXT_EVENT_STREAM_VALUE)) {
            log.debug("SSE 연결 중 예외 발생 (뷰 렌더링 방지): {}", ex.getMessage());
            return null;
        }

        ModelAndView mav = new ModelAndView("error/error2");
        
        String title = "시스템 오류.";
        String errorMessage = "죄송합니다.<br> <strong>요청을 처리할 수 없습니다.</strong>";
        
        mav.addObject("title", title);
        mav.addObject("message", errorMessage);

        log.info("INTERNAL_SERVER_ERROR 등 - ", ex);
        
        return mav;
    }
    
    @ExceptionHandler(IllegalArgumentException.class) //내 냉장고 비동기처리
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        log.info("BAD_REQUEST (IllegalArgument) - {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}