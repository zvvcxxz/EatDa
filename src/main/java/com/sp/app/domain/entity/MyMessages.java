package com.sp.app.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DIRECT_MESSAGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyMessages {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_DIRECT_MESSAGE_GEN")
    @SequenceGenerator(name = "SEQ_DIRECT_MESSAGE_GEN", sequenceName = "SEQ_DIRECT_MESSAGE", allocationSize = 1)
    @Column(name = "MESSAGE_ID")
    private Long id;

    @Column(name = "SENDER_ID", nullable = false)
    private Long senderId;

    @Column(name = "RECEIVER_ID", nullable = false)
    private Long receiverId;

    @Column(name = "SUBJECT", length = 200)
    private String subject;

    @Column(name = "CONTENT", length = 2000, nullable = false)
    private String content;

    @Column(name = "READ_AT")
    private LocalDateTime readAt;

    @Column(name = "SENDER_DELETED_YN", length = 1, nullable = false)
    private String senderDeletedYn = "N";

    @Column(name = "RECEIVER_DELETED_YN", length = 1, nullable = false)
    private String receiverDeletedYn = "N";

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MyMessages(Long senderId, Long receiverId, String subject, String content) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.subject = subject;
        this.content = content;
        this.senderDeletedYn = "N";
        this.receiverDeletedYn = "N";
    }

    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    public void deleteBySender() {
        this.senderDeletedYn = "Y";
    }

    public void deleteByReceiver() {
        this.receiverDeletedYn = "Y";
    }
}