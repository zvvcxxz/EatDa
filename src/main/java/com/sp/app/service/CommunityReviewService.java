package com.sp.app.service;

import java.util.List;
import java.util.Map;

import com.sp.app.domain.dto.CommunityReviewDto;

public interface CommunityReviewService {

    // 후기 목록과 검색 결과 개수
    public int dataCount(Map<String, Object> map);
    public List<CommunityReviewDto> listReview(Map<String, Object> map);

    // 후기 상세보기
    public CommunityReviewDto findById(long reviewId);

    // 사진을 포함한 후기 등록·수정
    public void insertReview(CommunityReviewDto dto) throws Exception;
    public int updateReview(CommunityReviewDto dto) throws Exception;

    // 본인 후기 삭제와 관리자 숨김
    public int deleteReview(Map<String, Object> map) throws Exception;
    public int hideReview(long reviewId) throws Exception;

    // 검색 조건에 맞는 이전글·다음글
    public CommunityReviewDto findByPrev(Map<String, Object> map);
    public CommunityReviewDto findByNext(Map<String, Object> map);
}