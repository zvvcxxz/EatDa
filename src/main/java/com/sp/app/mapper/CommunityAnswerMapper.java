package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.sp.app.domain.dto.CommunityAnswerDto;

@Mapper
public interface CommunityAnswerMapper {

    // 답변 등록
    public void insertAnswer(CommunityAnswerDto dto) throws SQLException;

    // 답변 개수와 목록
    public int answerCount(Map<String, Object> map);
    public List<CommunityAnswerDto> listAnswer(Map<String, Object> map);

    // 수정·삭제할 답변 조회
    public CommunityAnswerDto findById(long commentId);

    // 본인 답변 수정·삭제
    public int updateAnswer(CommunityAnswerDto dto) throws SQLException;
    public int deleteAnswer(Map<String, Object> map) throws SQLException;

    // 관리자 답변 숨김
    public int hideAnswer(Map<String, Object> map) throws SQLException;
}