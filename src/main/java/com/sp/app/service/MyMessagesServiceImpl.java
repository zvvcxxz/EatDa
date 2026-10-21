package com.sp.app.service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sp.app.domain.dto.MemberDto;
import com.sp.app.domain.dto.MyMessagesDto;
import com.sp.app.domain.entity.MyMessages;
import com.sp.app.mapper.MemberMapper;
import com.sp.app.repository.MyMessagesRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyMessagesServiceImpl implements MyMessagesService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final MyMessagesRepository myMessagesRepository; // 쪽지 기능: JPA
    private final MemberMapper memberMapper;                 // 회원 검색: MyBatis Mapper

    /**
     * MyBatis MemberMapper.listFindMember를 활용한 회원 검색
     */
    @Override
    public List<MyMessagesDto.MemberSearchResult> searchMembers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Object> map = new HashMap<>();
        map.put("kwd", keyword.trim());
        map.put("enabled", 1); // 정상 활동 상태인 회원만 검색 (STATUS = 1)

        // MemberMapper.xml의 listFindMember 실행
        List<MemberDto> memberList = memberMapper.listFindMember(map);

        return memberList.stream()
                .map(m -> {
                    // 화면에 표시할 수신자 이름 (예: 이름 또는 이름(아이디))
                    String displayName = (m.getName() != null && !m.getName().isEmpty())
                            ? m.getName() + " (" + m.getLogin_id() + ")"
                            : m.getLogin_id();

                    return new MyMessagesDto.MemberSearchResult(m.getMember_id(), displayName);
                })
                .toList();
    }

    @Override
    @Transactional
    public void sendMessages(Long senderId, MyMessagesDto.SendRequest request) {
        if (request.getRecipientIds() == null || request.getRecipientIds().isEmpty()) {
            throw new IllegalArgumentException("수신자를 최소 한 명 이상 선택해주세요.");
        }

        List<MyMessages> messagesToSave = new ArrayList<>();
        for (Long recipientId : request.getRecipientIds()) {
            if (senderId.equals(recipientId)) {
                continue; // 자기 자신 발송 제외 (CK_030)
            }
            MyMessages message = MyMessages.builder()
                    .senderId(senderId)
                    .receiverId(recipientId)
                    .subject(request.getTitle())
                    .content(request.getContent())
                    .build();
            messagesToSave.add(message);
        }

        if (!messagesToSave.isEmpty()) {
            myMessagesRepository.saveAll(messagesToSave);
        }
    }

    @Override
    public List<MyMessagesDto.Response> getReceivedMessages(Long currentUserId) {
        List<MyMessages> list = myMessagesRepository.findByReceiverIdAndReceiverDeletedYnOrderByCreatedAtDesc(currentUserId, "N");
        return list.stream().map(m -> convertToDto(m, currentUserId, true)).toList();
    }

    @Override
    public List<MyMessagesDto.Response> getSentMessages(Long currentUserId) {
        List<MyMessages> list = myMessagesRepository.findBySenderIdAndSenderDeletedYnOrderByCreatedAtDesc(currentUserId, "N");
        return list.stream().map(m -> convertToDto(m, currentUserId, false)).toList();
    }

    @Override
    @Transactional
    public MyMessagesDto.Response getMessageDetail(Long messageId, Long currentUserId) {
    	MyMessages message = myMessagesRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쪽지입니다."));

        if (!message.getSenderId().equals(currentUserId) && !message.getReceiverId().equals(currentUserId)) {
            throw new SecurityException("해당 쪽지를 열람할 권한이 없습니다.");
        }

        if (message.getReceiverId().equals(currentUserId)) {
            message.markAsRead();
        }

        return convertToDto(message, currentUserId, message.getReceiverId().equals(currentUserId));
    }

    @Override
    @Transactional
    public void readMessage(Long messageId, Long currentUserId) {
    	MyMessages message = myMessagesRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쪽지입니다."));

        if (message.getReceiverId().equals(currentUserId)) {
            message.markAsRead();
        }
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long currentUserId) {
    		MyMessages message = myMessagesRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쪽지입니다."));

        if (message.getSenderId().equals(currentUserId)) {
            message.deleteBySender();
        } else if (message.getReceiverId().equals(currentUserId)) {
            message.deleteByReceiver();
        } else {
            throw new SecurityException("삭제 권한이 없습니다.");
        }
    }

    
    private String getMemberName(Long memberId) {
        if (memberId == null) {
            return "알 수 없음";
        }

        MemberDto member = memberMapper.findById(memberId);

        if (member == null) {
            return "탈퇴한 회원";
        }

        // 이름(NAME)이 존재하면 이름 반환, 없으면 로그인 아이디(LOGIN_ID) 반환
        if (member.getName() != null && !member.getName().trim().isEmpty()) {
            return member.getName();
        } else if (member.getLogin_id() != null && !member.getLogin_id().trim().isEmpty()) {
            return member.getLogin_id();
        }

        return "회원_" + memberId;
    }

    private MyMessagesDto.Response convertToDto(MyMessages entity, Long currentUserId, boolean isReceived) {

        String senderNickname = getMemberName(entity.getSenderId());
        String recipientNickname = getMemberName(entity.getReceiverId());
        String counterpart = isReceived ? senderNickname : recipientNickname;

        String previewText = entity.getContent();
        if (previewText != null && previewText.length() > 30) {
            previewText = previewText.substring(0, 30) + "...";
        }

        return MyMessagesDto.Response.builder()
                .id(entity.getId())
                .senderId(entity.getSenderId())
                .senderNickname(senderNickname)
                .recipientId(entity.getReceiverId())
                .recipientNickname(recipientNickname)
                .counterpartNickname(counterpart)
                .title(entity.getSubject())
                .preview(previewText)
                .content(entity.getContent())
                .read(entity.getReadAt() != null)
                .readAtText(entity.getReadAt() != null ? entity.getReadAt().format(DATE_FORMATTER) : null)
                .createdAtText(entity.getCreatedAt() != null ? entity.getCreatedAt().format(DATE_FORMATTER) : "")
                .build();
    }
    
    @Override
    @Transactional
    public void cancelMessage(Long messageId, Long currentUserId) {
        MyMessages message = myMessagesRepository.findById(messageId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 쪽지입니다."));

        // 본인이 보낸 쪽지인지 검증
        if (!message.getSenderId().equals(currentUserId)) {
            throw new SecurityException("보낸 사람만 발송 취소할 수 있습니다.");
        }

        if (message.getReadAt() != null) {
            throw new IllegalStateException("상대방이 이미 읽은 쪽지는 발송 취소할 수 없습니다.");
        }
        // DB에서 데이터 자체를 완전 삭제 (상대방 쪽지함에서도 삭제됨)
        myMessagesRepository.delete(message);
    }
}