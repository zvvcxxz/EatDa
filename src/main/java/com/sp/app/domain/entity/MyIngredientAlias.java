package com.sp.app.domain.entity;

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
@Table(name = "INGREDIENT_ALIAS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyIngredientAlias {
	
	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
        generator = "ingredient_alias_seq")
    @SequenceGenerator( name = "ingredient_alias_seq",
        sequenceName = "SEQ_INGREDIENT_ALIAS", allocationSize = 1)
    @Column(name = "ALIAS_ID")
    private Long aliasId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INGREDIENT_ID", nullable = false)
    private MyIngredient ingredient;

    @Column(name = "ALIAS_NAME", nullable = false, length = 100, unique = true)
    private String aliasName;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn;

}
