package com.rofix.fitspot.webservice.api.user.repository;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothingRepository extends JpaRepository<Clothing, Long> {
    List<Clothing> findByUserUserId(Long userid);

    // 추천을 위한 의상 조회 (사용자별, 카테고리별, 날씨별)
    @Query("SELECT c FROM Clothing c " +
            "WHERE c.user.userId = :userId " +
            "AND c.category = :category " +
            "AND (c.weather = :weather OR c.weather = 'ALL') " +
            "ORDER BY " +
            "CASE WHEN c.weather = :weather THEN 1 ELSE 2 END, " +
            "c.createdAt DESC")
    List<Clothing> findByUserIdAndCategoryAndWeather(@Param("userId") Long userId,
                                                     @Param("category") String category,
                                                     @Param("weather") String weather);

    // 사용자의 모든 의상을 카테고리별로 조회 (날씨 상관없이)
    @Query("SELECT c FROM Clothing c " +
            "WHERE c.user.userId = :userId " +
            "AND c.category = :category " +
            "ORDER BY c.createdAt DESC")
    List<Clothing> findByUserIdAndCategory(@Param("userId") Long userId,
                                           @Param("category") String category);

    // 사용자의 최근 좋아요한 옷들의 카테고리 조회 (개인화를 위해)
    @Query("SELECT c.category, COUNT(c.category) as cnt FROM Clothing c " +
            "WHERE c.user.userId = :userId " +
            "GROUP BY c.category " +
            "ORDER BY cnt DESC")
    List<Object[]> findFavoriteCategories(@Param("userId") Long userId);
}