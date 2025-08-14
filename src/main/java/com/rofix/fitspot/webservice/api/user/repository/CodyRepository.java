package com.rofix.fitspot.webservice.api.user.repository;

import com.rofix.fitspot.webservice.api.user.entity.Cody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodyRepository extends JpaRepository<Cody, Long> {

    // 제목으로 검색 (날씨 필터 포함) - 수정됨
    @Query("SELECT DISTINCT c, " +
            "(SELECT COUNT(l) FROM Like l WHERE l.cody.codyId = c.codyId) as likeCount, " +
            "(SELECT COUNT(com) FROM Comment com WHERE com.cody.codyId = c.codyId) as commentCount " +
            "FROM Cody c " +
            "WHERE (:searchText IS NULL OR c.title LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.weather = :category)")
    List<Object[]> searchByTitle(@Param("searchText") String searchText,
                                 @Param("category") String category);

    // 설명으로 검색 (날씨 필터 포함) - 수정됨
    @Query("SELECT DISTINCT c, " +
            "(SELECT COUNT(l) FROM Like l WHERE l.cody.codyId = c.codyId) as likeCount, " +
            "(SELECT COUNT(com) FROM Comment com WHERE com.cody.codyId = c.codyId) as commentCount " +
            "FROM Cody c " +
            "WHERE (:searchText IS NULL OR c.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.weather = :category)")
    List<Object[]> searchByDescription(@Param("searchText") String searchText,
                                       @Param("category") String category);

    // 제목과 설명 모두에서 검색 (날씨 필터 포함) - 수정됨
    @Query("SELECT DISTINCT c, " +
            "(SELECT COUNT(l) FROM Like l WHERE l.cody.codyId = c.codyId) as likeCount, " +
            "(SELECT COUNT(com) FROM Comment com WHERE com.cody.codyId = c.codyId) as commentCount " +
            "FROM Cody c " +
            "WHERE (:searchText IS NULL OR c.title LIKE %:searchText% OR c.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.weather = :category)")
    List<Object[]> searchByTitleAndDescription(@Param("searchText") String searchText,
                                               @Param("category") String category);

    // 코디에 포함된 옷들의 카테고리 목록 조회 (기존 유지)
    @Query("SELECT DISTINCT cl.category FROM Cody c " +
            "JOIN CodyClothes cc ON c.codyId = cc.cody.codyId " +
            "JOIN Clothing cl ON cc.clothing.clothingId = cl.clothingId " +
            "WHERE cl.category IS NOT NULL ORDER BY cl.category")
    List<String> findDistinctClothingCategories();

    // 사용 가능한 날씨 카테고리 목록 조회 - 새로 추가
    @Query("SELECT DISTINCT c.weather FROM Cody c " +
            "WHERE c.weather IS NOT NULL ORDER BY c.weather")
    List<String> findDistinctWeatherCategories();

    // 특정 코디에 포함된 옷들 조회 (기존 유지)
    @Query("SELECT cl FROM Cody c " +
            "JOIN CodyClothes cc ON c.codyId = cc.cody.codyId " +
            "JOIN Clothing cl ON cc.clothing.clothingId = cl.clothingId " +
            "WHERE c.codyId = :codyId")
    List<Object> findClothingsByCodyId(@Param("codyId") Long codyId);

    // 해시로 기존 코디 조회 (중복 방지용) - 기존 유지
    @Query("SELECT c FROM Cody c WHERE c.hash = :hash")
    Optional<Cody> findByHash(@Param("hash") String hash);

    // 사용자와 날씨로 기존 코디 조회 - 기존 유지
    @Query("SELECT c FROM Cody c WHERE c.user.userId = :userId AND c.weather = :weather ORDER BY c.createdAt DESC")
    List<Cody> findByUserIdAndWeather(@Param("userId") Long userId, @Param("weather") String weather);
}