package com.sp.app.mapper;

import java.sql.SQLException;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.sp.app.domain.dto.TogetherMeetCommentDto;
import com.sp.app.domain.dto.TogetherMeetDto;
import com.sp.app.domain.dto.TogetherMeetParticipantDto;

// 같이 먹어요 : 게시글 정보
@Mapper
public interface TogetherMeetMapper {
    // 같이 먹어요 : 모임 글과 입력 정보 저장
    public void insertMeet(TogetherMeetDto dto) throws SQLException; // 같이먹어요 글 작성

    // 같이 먹어요 : 모임 글 목록 조회

    public List<TogetherMeetDto> listMeet(@Param("offset") int offset, @Param("size") int size); // 같이먹어요 글 목록(더보기)

    // 같이 먹어요 : 모임 글 수정

    public int updateMeet(TogetherMeetDto dto) throws SQLException; // 같이 먹어요 글 수정

    // 같이 먹어요 : 모임 글 상세 조회

    public TogetherMeetDto findMeetById(Long meet_id); // 같이 먹어요 글 상세보기

    // 같이 먹어요 : 모임 글 삭제

    public int deleteMeet(@Param("meet_id") Long meet_id, @Param("author_id") Long author_id)
            throws Exception; // 같이 먹어요 글 삭제

    // 같이 먹어요 : 게시글 조회수 증가

    public void updateViewCount(@Param("meet_id") Long meet_id); // 글 상세보기 - 조회수 증가

    // 같이 먹어요 : 승인된 참여 인원 조회

    public int countMeetParticipants(@Param("meet_id") Long meet_id); // 글 상세보기 - 참여 현황

    // 같이 먹어요 : 참여 신청 저장

    public int createMeetParticipant(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id); // 같이먹어요 참여 신청

    // 같이 먹어요 : 참여 신청자 목록 조회

    public List<TogetherMeetParticipantDto> listParticipant(@Param("meet_id") Long meet_id); // 같이먹어요 신청자 목록

    // 같이 먹어요 : 참여 인원과 권한 확인 후 승인

    public int approveMeetParticipant(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id); // 참여 신청 승인

    // 같이 먹어요 : 작성자 권한 확인 후 신청 거절

    public int rejectMeetParticipant(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id); // 참여 신청 거절

    // 같이 먹어요 : 중복 참여 신청 확인

    public int countMeetApplication(@Param("meet_id") Long meet_id, @Param("member_id") Long member_id); // 참여 신청 중복 확인

    // 같이 먹어요 : 승인된 참여자 목록 조회

    public List<TogetherMeetParticipantDto> listAcceptedParticipants(@Param("meet_id") Long meet_id); // 현재 함께하는 이웃 + 승인된 참여자

    // 같이먹어요 댓글
    public void insertMeetComment(TogetherMeetCommentDto dto) throws Exception; // 같이 먹어요 댓글 등록

    // 같이 먹어요 : 댓글 목록 조회

    public List<TogetherMeetCommentDto> listMeetComment(@Param("meet_id") Long meet_id,
            @Param("offset") int offset, @Param("size") int size); // 같이 먹어요 댓글 목록

    // 같이 먹어요 : 삭제되지 않은 댓글 수 조회

    public int countMeetComments(@Param("meet_id") Long meet_id); // 삭제 되지 않은 전체 댓글 수
}
