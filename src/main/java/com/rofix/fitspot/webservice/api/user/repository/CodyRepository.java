package com.rofix.fitspot.webservice.api.user.repository;

import com.rofix.fitspot.webservice.api.user.entity.Cody;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodyRepository extends JpaRepository<Cody, Long> {

    // 제목으로 검색 (카테고리 필터 포함)
    @Query("SELECT DISTINCT c, " +
            "(SELECT COUNT(l) FROM Like l WHERE l.cody.codyId = c.codyId) as likeCount, " +
            "(SELECT COUNT(com) FROM Comment com WHERE com.cody.codyId = c.codyId) as commentCount " +
            "FROM Cody c " +
            "LEFT JOIN CodyClothes cc ON c.codyId = cc.cody.codyId " +
            "LEFT JOIN Clothing cl ON cc.clothing.clothingId = cl.clothingId " +
            "WHERE (:searchText IS NULL OR c.title LIKE %:searchText%) AND " +
            "(:category IS NULL OR cl.category = :category)")
    List<Object[]> searchByTitle(@Param("searchText") String searchText,
                                 @Param("category") String category);

    // 설명으로 검색 (카테고리 필터 포함)
    @Query("SELECT DISTINCT c, " +
            "(SELECT COUNT(l) FROM Like l WHERE l.cody.codyId = c.codyId) as likeCount, " +
            "(SELECT COUNT(com) FROM Comment com WHERE com.cody.codyId = c.codyId) as commentCount " +
            "FROM Cody c " +
            "LEFT JOIN CodyClothes cc ON c.codyId = cc.cody.codyId " +
            "LEFT JOIN Clothing cl ON cc.clothing.clothingId = cl.clothingId " +
            "WHERE (:searchText IS NULL OR c.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR cl.category = :category)")
    List<Object[]> searchByDescription(@Param("searchText") String searchText,
                                       @Param("category") String category);

    // 제목과 설명 모두에서 검색 (카테고리 필터 포함)
    @Query("SELECT DISTINCT c, " +
            "(SELECT COUNT(l) FROM Like l WHERE l.cody.codyId = c.codyId) as likeCount, " +
            "(SELECT COUNT(com) FROM Comment com WHERE com.cody.codyId = c.codyId) as commentCount " +
            "FROM Cody c " +
            "LEFT JOIN CodyClothes cc ON c.codyId = cc.cody.codyId " +
            "LEFT JOIN Clothing cl ON cc.clothing.clothingId = cl.clothingId " +
            "WHERE (:searchText IS NULL OR c.title LIKE %:searchText% OR c.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR cl.category = :category)")
    List<Object[]> searchByTitleAndDescription(@Param("searchText") String searchText,
                                               @Param("category") String category);

    // 코디에 포함된 옷들의 카테고리 목록 조회
    @Query("SELECT DISTINCT cl.category FROM Cody c " +
            "JOIN CodyClothes cc ON c.codyId = cc.cody.codyId " +
            "JOIN Clothing cl ON cc.clothing.clothingId = cl.clothingId " +
            "WHERE cl.category IS NOT NULL ORDER BY cl.category")
    List<String> findDistinctClothingCategories();

    // 특정 코디에 포함된 옷들 조회
    @Query("SELECT cl FROM Cody c " +
            "JOIN CodyClothes cc ON c.codyId = cc.cody.codyId " +
            "JOIN Clothing cl ON cc.clothing.clothingId = cl.clothingId " +
            "WHERE c.codyId = :codyId")
    List<Object> findClothingsByCodyId(@Param("codyId") Long codyId);
}