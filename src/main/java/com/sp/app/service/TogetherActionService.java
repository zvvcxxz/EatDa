package com.sp.app.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.sp.app.mapper.TogetherActionMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TogetherActionService {
    private final TogetherActionMapper mapper;

    private Map<String, Object> post(String type, long id, boolean lock) {
        if ((!"meet".equals(type) && !"share".equals(type)) || id < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "글 번호가 올바르지 않습니다.");
        }
        Map<String, Object> post = lock ? mapper.lockPost(type, id) : mapper.findPost(type, id);
        if (post == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제되었거나 없는 글입니다.");
        return post;
    }

    private long number(Map<String, Object> row, String name) {
        return ((Number) row.get(name)).longValue();
    }

    // 좋아요와 신청 : 로그인한 회원 기준으로 상태 조회
    @Transactional(transactionManager = "oracleTransactionManager", readOnly = true)
    public Map<String, Object> state(String type, long id, Long member) {
        Map<String, Object> post = post(type, id, false);
        boolean owner = member != null && number(post, "AUTHOR_ID") == member;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("owner", owner);
        result.put("likes", mapper.countLikes(type, id));
        result.put("liked", member != null && mapper.liked(type, id, member) > 0);
        result.put("application", member == null ? null : mapper.applicationStatus(type, id, member));
        if ("share".equals(type)) {
            result.put("applicants", member == null ? java.util.List.of() : mapper.shareApplicants(id, owner, member));
            result.put("accepted", mapper.acceptedCount(id));
            result.put("capacity", number(post, "CAPACITY"));
        }
        return result;
    }

    // 좋아요 : 같은 글을 잠근 뒤 회원별 좋아요를 켜거나 끔
    @Transactional(transactionManager = "oracleTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> like(String type, long id, long member) {
        post(type, id, true);
        if (mapper.liked(type, id, member) > 0) mapper.removeLike(type, id, member);
        else mapper.addLike(type, id, member);
        return state(type, id, member);
    }

    // 나눔 조회수 : 상세 화면에 들어왔을 때만 증가
    @Transactional(transactionManager = "oracleTransactionManager")
    public void view(long id) {
        post("share", id, false);
        mapper.increaseShareViews(id);
    }

    // 나눔 신청 : 작성자 본인, 중복 신청, 마감과 정원 확인
    @Transactional(transactionManager = "oracleTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> apply(long id, long member) {
        Map<String, Object> post = post("share", id, true);
        if (number(post, "AUTHOR_ID") == member) fail("본인 글에는 신청할 수 없습니다.");
        if (number(post, "CLOSED") != 0) fail("신청이 마감된 글입니다.");
        if (mapper.applicationStatus("share", id, member) != null) fail("이미 신청한 글입니다.");
        if (mapper.acceptedCount(id) >= number(post, "CAPACITY")) fail("수령 인원이 모두 찼습니다.");
        mapper.applyShare(id, member);
        return state("share", id, member);
    }

    // 나눔 승인 : 작성자만 처리하고 동시 승인도 정원 안에서만 허용
    @Transactional(transactionManager = "oracleTransactionManager", rollbackFor = Exception.class)
    public Map<String, Object> decide(long id, long applicant, long member, String decision) {
        Map<String, Object> post = post("share", id, true);
        if (number(post, "AUTHOR_ID") != member) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "작성자만 처리할 수 있습니다.");
        if (!"accept".equals(decision) && !"reject".equals(decision)) fail("처리 유형이 올바르지 않습니다.");
        Integer status = mapper.applicationStatus("share", id, applicant);
        if (status == null || status != 0) fail("대기 중인 신청이 아닙니다.");
        if ("accept".equals(decision)) {
            if (number(post, "CLOSED") != 0) fail("마감된 글은 승인할 수 없습니다.");
            if (mapper.acceptedCount(id) >= number(post, "CAPACITY")) fail("수령 인원이 모두 찼습니다.");
        }
        if (mapper.decideShare(id, applicant, "accept".equals(decision) ? 1 : 2) != 1) fail("이미 처리된 신청입니다.");
        return state("share", id, member);
    }

    private void fail(String message) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, message);
    }
}
