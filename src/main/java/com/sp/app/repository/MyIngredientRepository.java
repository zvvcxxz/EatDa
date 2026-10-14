package com.sp.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.domain.entity.MyIngredient;

public interface MyIngredientRepository extends JpaRepository<MyIngredient, Long> {

    // 재료명 또는 동의어로 검색 (FETCH JOIN 적용)
    @Query("SELECT DISTINCT i FROM MyIngredient i " +
           "LEFT JOIN FETCH i.aliases a " +
           "WHERE i.activeYn = 'Y' AND " +
           "(LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "(a.activeYn = 'Y' AND LOWER(a.aliasName) LIKE LOWER(CONCAT('%', :keyword, '%'))))")
    List<MyIngredient> searchByNameOrAlias(@Param("keyword") String keyword);

    // 카테고리(타입)별 재료 목록 조회
    List<MyIngredient> findByTypeAndActiveYn(String type, String activeYn);
}