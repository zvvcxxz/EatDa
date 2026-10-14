package com.sp.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.domain.entity.MemberFridge;

public interface MemberFridgeRepository extends JpaRepository<MemberFridge, Long> {

    // 검색/필터 적용 + 최근 등록순(createdAt DESC) 최대 30개 조회
    @Query("SELECT mf FROM MemberFridge mf JOIN FETCH mf.ingredient i " +
           "WHERE mf.memberId = :memberId " +
           "AND (:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR i.type = :categoryId) " +
           "ORDER BY mf.createdAt DESC")
    List<MemberFridge> findTop30Items(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            Pageable pageable
    );

    // 단건 상세 조회 (소유권 검증용)
    Optional<MemberFridge> findByIdAndMemberId(Long id, Long memberId);

    // 중복 등록 방지 및 수량 합산용 조회
    Optional<MemberFridge> findByMemberIdAndIngredient_Id(Long memberId, Long ingredientId);

    // 특정 회원의 냉장고 품목 전체 조회 (재료 정보와 함께 Fetch Join)
    @Query("SELECT mf FROM MemberFridge mf JOIN FETCH mf.ingredient WHERE mf.memberId = :memberId ORDER BY mf.createdAt DESC")
    List<MemberFridge> findAllByMemberIdWithIngredient(@Param("memberId") Long memberId);
}
