package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.TogetherMeetCommentDto;
import com.sp.app.domain.dto.TogetherMeetDto;
import com.sp.app.domain.dto.TogetherMeetParticipantDto;

// 같이 먹어요 
public interface TogetherMeetService {
	public void insertMeet(TogetherMeetDto dto) throws Exception; // 글 등록
	
	public List<TogetherMeetDto> listMeet() throws Exception; // 글 목록
	
	public int updateMeet(TogetherMeetDto dto) throws Exception; // 글 수정
	
	public TogetherMeetDto findMeetById(Long meet_id) throws Exception; // 글 상세보기 
	
	public int countMeetParticipants(Long meet_id) throws Exception; // 글 상세보기 - 참여 현황
	
	public int createMeetParticipants(Long meet_id, Long member_id) throws Exception; // 글 상세보기 - 참여 신청
	
	public List<TogetherMeetParticipantDto> listParticipant(Long meet_id) throws Exception; // 참여자 목록
	
	public int approveMeetParticipant(Long meet_id, Long member_id, Long author_id) throws Exception; // 참여 신청 승인
	
	public int rejectMeetParticipant(Long meet_id, Long member_id, Long author_id) throws Exception; // 참여 신청 거절
	
	public List<TogetherMeetParticipantDto> listAcceptedParticipants(Long meet_id) throws Exception; // 현재 함께하는 이웃 : 방장 + 승인된 참여자
	
	public void insertMeetComment(TogetherMeetCommentDto dto) throws Exception; // 같이먹어요 댓글 등록
	
	public List<TogetherMeetCommentDto> listMeetComment(Long meet_id) throws Exception; // 같이먹어요 댓글 목록
}
