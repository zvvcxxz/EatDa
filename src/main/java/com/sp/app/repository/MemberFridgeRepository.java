package com.sp.app.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sp.app.domain.entity.MemberFridge;

public interface MemberFridgeRepository extends JpaRepository<MemberFridge, Long> {

    // 검색/필터 적용 + 최근 등록순(createdAt DESC)
    @Query("SELECT mf FROM MemberFridge mf JOIN FETCH mf.ingredient i " +
           "WHERE mf.memberId = :memberId " +
           "AND (:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR i.type = :categoryId) " +
           "ORDER BY mf.createdAt DESC")
    List<MemberFridge> findAllItems(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId,
            Pageable pageable
    );

    //검색/필터 조건에 알맞은 '전체 재료 개수' 계산 쿼리
    @Query("SELECT COUNT(mf) FROM MemberFridge mf JOIN mf.ingredient i " +
           "WHERE mf.memberId = :memberId " +
           "AND (:keyword IS NULL OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR i.type = :categoryId)")
    long countFilteredItems(
            @Param("memberId") Long memberId,
            @Param("keyword") String keyword,
            @Param("categoryId") String categoryId
    );
    
    // 단건 상세 조회 (소유권 검증용)
    Optional<MemberFridge> findByIdAndMemberId(Long id, Long memberId);

    // 재료 ID와 유통기한이 모두 일치하는 항목 조회 (중복 합산용)
    Optional<MemberFridge> findByMemberIdAndIngredient_IdAndExpiresOn(Long memberId, Long ingredientId, LocalDate expiresOn);
    
    // 유통기한이 null인 항목 중 중복 조회
    Optional<MemberFridge> findByMemberIdAndIngredient_IdAndExpiresOnIsNull(Long memberId, Long ingredientId);
    
    // 특정 회원의 냉장고 품목 전체 조회 (재료 정보와 함께 Fetch Join)
    @Query("SELECT mf FROM MemberFridge mf JOIN FETCH mf.ingredient WHERE mf.memberId = :memberId ORDER BY mf.createdAt DESC")
    List<MemberFridge> findAllByMemberIdWithIngredient(@Param("memberId") Long memberId);
}
