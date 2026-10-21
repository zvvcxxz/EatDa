package com.sp.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sp.app.domain.entity.MyMessages;

public interface MyMessagesRepository extends JpaRepository<MyMessages, Long> {

    // 받은 쪽지함 조회 (수신자 미삭제건)
    List<MyMessages> findByReceiverIdAndReceiverDeletedYnOrderByCreatedAtDesc(Long receiverId, String receiverDeletedYn);

    // 보낸 쪽지함 조회 (발신자 미삭제건)
    List<MyMessages> findBySenderIdAndSenderDeletedYnOrderByCreatedAtDesc(Long senderId, String senderDeletedYn);
}