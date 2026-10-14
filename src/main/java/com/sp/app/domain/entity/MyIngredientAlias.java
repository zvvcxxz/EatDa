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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "INGREDIENT_ALIAS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyIngredientAlias {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INGREDIENT_ALIAS")
    @SequenceGenerator(name = "SEQ_INGREDIENT_ALIAS", sequenceName = "SEQ_INGREDIENT_ALIAS", allocationSize = 1)
    @Column(name = "ALIAS_ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "INGREDIENT_ID", nullable = false)
    private MyIngredient ingredient;

    @Column(name = "ALIAS_NAME", nullable = false, length = 100, unique = true)
    private String aliasName;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn = "Y";
}
