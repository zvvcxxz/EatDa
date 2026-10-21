package com.sp.app.service;

import java.util.List;

import com.sp.app.domain.dto.MyMessagesDto;

public interface MyMessagesService {
	List<MyMessagesDto.MemberSearchResult> searchMembers(String keyword);
	void sendMessages(Long senderId, MyMessagesDto.SendRequest request);
    List<MyMessagesDto.Response> getReceivedMessages(Long currentUserId);
    List<MyMessagesDto.Response> getSentMessages(Long currentUserId);
    MyMessagesDto.Response getMessageDetail(Long messageId, Long currentUserId);
    void readMessage(Long messageId, Long currentUserId);
    void deleteMessage(Long messageId, Long currentUserId);
    void cancelMessage(Long messageId, Long currentUserId);
}
