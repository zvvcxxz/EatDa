package com.sp.app.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEMBER_FRIDGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MemberFridge {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "member_fridge_seq"
    )
    @SequenceGenerator(
        name = "member_fridge_seq",
        sequenceName = "SEQ_MEMBER_FRIDGE",
        allocationSize = 1
    )
    @Column(name = "FRIDGE_ITEM_ID")
    private Long fridgeItemId;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "INGREDIENT_ID",
        nullable = false
    )
    private MyIngredient ingredient;

    @Column(name = "QUANTITY", precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "UNIT", length = 20)
    private String unit;

    @Column(name = "EXPIRES_ON")
    private LocalDate expiresOn;

    @Column(name = "MEMO", length = 300)
    private String memo;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;
}
