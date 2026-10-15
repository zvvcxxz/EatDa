package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.CommunityReviewDto;

@Mapper
public interface CommunityReviewMapper {

    // 후기 목록과 검색 결과 개수
    public int dataCount(Map<String, Object> map);
    public List<CommunityReviewDto> listReview(Map<String, Object> map);

    // 후기 상세보기
    public CommunityReviewDto findById(long reviewId);

    // 로그인 회원의 후기 등록·수정·삭제
    public void insertReview(CommunityReviewDto dto) throws SQLException;
    public int updateReview(CommunityReviewDto dto) throws SQLException;
    public int deleteReview(Map<String, Object> map) throws SQLException;

    // 관리자 숨김
    public int hideReview(long reviewId) throws SQLException;

    // 검색 조건에 맞는 이전글·다음글
    public CommunityReviewDto findByPrev(Map<String, Object> map);
    public CommunityReviewDto findByNext(Map<String, Object> map);
}