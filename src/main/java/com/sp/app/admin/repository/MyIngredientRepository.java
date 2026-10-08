package com.sp.app.admin.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.domain.entity.MyIngredient;

public interface MyIngredientRepository extends JpaRepository<MyIngredient, Long> {

    @Query("""
        SELECT DISTINCT i
        FROM MyIngredient i
        LEFT JOIN i.aliases a
        WHERE i.activeYn = 'Y'
          AND (LOWER(i.ingredientName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR
                (a.activeYn = 'Y' AND LOWER(a.aliasName) LIKE LOWER(CONCAT('%', :keyword, '%')) ) )
        ORDER BY i.ingredientName  """)
    List<MyIngredient> searchIngredients( @Param("keyword") String keyword );
    
}
