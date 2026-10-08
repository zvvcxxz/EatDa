package com.sp.app.domain.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "INGREDIENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class MyIngredient {

    @Id
    @GeneratedValue( strategy = GenerationType.SEQUENCE, 
    	generator = "ingredient_seq" )
    @SequenceGenerator( name = "ingredient_seq",
        sequenceName = "SEQ_INGREDIENT", allocationSize = 1 )
    @Column(name = "INGREDIENT_ID")
    private Long ingredientId;

    @Column(name = "INGREDIENT_NAME", nullable = false, length = 100, unique = true)
    private String ingredientName;

    @Column(name = "INGREDIENT_TYPE", length = 20)
    private String ingredientType;

    @Column(name = "DEFAULT_UNIT", length = 20)
    private String defaultUnit;

    @Column(name = "ACTIVE_YN", nullable = false, length = 1)
    private String activeYn;

    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany( mappedBy = "ingredient", fetch = FetchType.LAZY )
    @Builder.Default
    private List<MyIngredientAlias> aliases = new ArrayList<>();
	
}
