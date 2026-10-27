package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.TogetherMeetCommentDto;
import com.sp.app.domain.dto.TogetherMeetDto;
import com.sp.app.domain.dto.TogetherMeetParticipantDto;

// 같이 먹어요
public interface TogetherMeetService {
    // 같이 먹어요 : 모임 글과 입력 정보 저장
    public void insertMeet(TogetherMeetDto dto) throws Exception; // 글 등록

    // 같이 먹어요 : 모임 글 목록 조회

    public List<TogetherMeetDto> listMeet(int offset, int size) throws Exception; // 글 목록 (더보기)

    // 같이 먹어요 : 모임 글 수정

    public int updateMeet(TogetherMeetDto dto) throws Exception; // 글 수정

    // 같이 먹어요 : 모임 글 삭제

    public int deleteMeet(Long meet_id, Long author_id) throws Exception; // 글 삭제

    // 같이 먹어요 : 모임 글 상세 조회

    public TogetherMeetDto findMeetById(Long meet_id) throws Exception; // 글 상세보기

    // 같이 먹어요 : 승인된 참여 인원 조회

    public int countMeetParticipants(Long meet_id) throws Exception; // 글 상세보기 - 참여 현황

    // 같이 먹어요 : 참여 신청 조건 확인 후 저장

    public int createMeetParticipants(Long meet_id, Long member_id) throws Exception; // 글 상세보기 - 참여 신청

    // 같이 먹어요 : 참여 신청자 목록 조회

    public List<TogetherMeetParticipantDto> listParticipant(Long meet_id) throws Exception; // 참여자 목록

    // 같이 먹어요 : 참여 인원과 권한 확인 후 승인

    public int approveMeetParticipant(Long meet_id, Long member_id, Long author_id) throws Exception; // 참여 신청 승인

    // 같이 먹어요 : 작성자 권한 확인 후 신청 거절

    public int rejectMeetParticipant(Long meet_id, Long member_id, Long author_id) throws Exception; // 참여 신청 거절

    // 같이 먹어요 : 승인된 참여자 목록 조회

    public List<TogetherMeetParticipantDto> listAcceptedParticipants(Long meet_id) throws Exception; // 현재 함께하는 이웃 : 방장 + 승인된 참여자

    // 같이 먹어요 : 댓글 저장

    public void insertMeetComment(TogetherMeetCommentDto dto) throws Exception; // 같이먹어요 댓글 등록

    // 같이 먹어요 : 댓글 목록 조회

    public List<TogetherMeetCommentDto> listMeetComment(Long meet_id, int offset, int size) throws Exception; // 같이먹어요 댓글 목록

    // 같이 먹어요 : 삭제되지 않은 댓글 수 조회

    public int countMeetComments(Long meet_id) throws Exception; // 같이 먹어요 전체 댓글 수
}
