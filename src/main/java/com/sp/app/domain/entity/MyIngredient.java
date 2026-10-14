package com.sp.app.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "INGREDIENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MyIngredient {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQ_INGREDIENT")
    @SequenceGenerator(name = "SEQ_INGREDIENT", sequenceName = "SEQ_INGREDIENT", allocationSize = 1)
    @Column(name = "INGREDIENT_ID")
    private Long id;

    @Column(name = "INGREDIENT_NAME", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "INGREDIENT_TYPE", length = 20)
    private String type; // MEAT, SEAFOOD, EGG 등

    @Column(name = "DEFAULT_UNIT", length = 20)
    private String defaultUnit;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn = "Y";

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "ingredient", cascade = CascadeType.ALL)
    private List<MyIngredientAlias> aliases = new ArrayList<>();

}
