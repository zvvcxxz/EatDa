package com.sp.app.domain.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "MEMBER_FRIDGE")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberFridge {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_MEMBER_FRIDGE")
    @SequenceGenerator(name = "SEQ_MEMBER_FRIDGE", sequenceName = "SEQ_MEMBER_FRIDGE", allocationSize = 1)
    @Column(name = "FRIDGE_ITEM_ID")
    private Long id;

    @Column(name = "MEMBER_ID", nullable = false)
    private Long memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INGREDIENT_ID", nullable = false)
    private MyIngredient ingredient;

    @Column(name = "QUANTITY", precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "UNIT", length = 20)
    private String unit;

    @Column(name = "EXPIRES_ON")
    private LocalDate expiresOn;

    @Column(name = "MEMO", length = 300)
    private String memo;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public MemberFridge(Long memberId, MyIngredient ingredient, BigDecimal quantity, String unit, LocalDate expiresOn, String memo) {
        this.memberId = memberId;
        this.ingredient = ingredient;
        this.quantity = quantity;
        this.unit = unit != null ? unit : (ingredient != null ? ingredient.getDefaultUnit() : null);
        this.expiresOn = expiresOn;
        this.memo = memo;
    }
    
    public void updateDetails(BigDecimal quantity, String unit, LocalDate expiresOn, String memo) {
        this.quantity = quantity;
        this.unit = unit;
        this.expiresOn = expiresOn;
        this.memo = memo;
    }
}
