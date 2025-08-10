package com.rofix.fitspot.webservice.api.user.repository;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothingRepository extends JpaRepository<Clothing, Long> {
    List<Clothing> findByUserUserId(Long userid);

    @Query("SELECT c FROM Clothing c WHERE " +
            "(:searchText IS NULL OR c.title LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.category = :category)")
    List<Clothing> findByTitleContainingAndCategory(@Param("searchText") String searchText,
                                                    @Param("category") String category,
                                                    Sort sort);

    @Query("SELECT c FROM Clothing c WHERE " +
            "(:searchText IS NULL OR c.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.category = :category)")
    List<Clothing> findByDescriptionContainingAndCategory(@Param("searchText") String searchText,
                                                          @Param("category") String category,
                                                          Sort sort);

    @Query("SELECT c FROM Clothing c WHERE " +
            "(:searchText IS NULL OR c.title LIKE %:searchText% OR c.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.category = :category)")
    List<Clothing> findByTitleOrDescriptionContainingAndCategory(@Param("searchText") String searchText,
                                                                 @Param("category") String category,
                                                                 Sort sort);

    List<Clothing> findByCategory(String category, Sort sort);

    @Query("SELECT DISTINCT c.category FROM Clothing c WHERE c.category IS NOT NULL ORDER BY c.category")
    List<String> findDistinctCategories();

    @Query("SELECT c, COUNT(l) as likeCount FROM Clothing c " +
            "LEFT JOIN CodyClothes cc ON c.clothingId = cc.clothing.clothingId " +
            "LEFT JOIN Like l ON cc.cody.codyId = l.cody.codyId " +
            "WHERE (:searchText IS NULL OR c.title LIKE %:searchText% OR c.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.category = :category) " +
            "GROUP BY c.clothingId " +
            "ORDER BY likeCount DESC")
    List<Object[]> findClothingWithLikeCount(@Param("searchText") String searchText,
                                             @Param("category") String category);


    // Cody 테이블을 통한 의상 검색 - 제목 기준
    @Query("SELECT DISTINCT c, COUNT(l) as likeCount FROM Clothing c " +
            "JOIN CodyClothes cc ON c.clothingId = cc.clothing.clothingId " +
            "JOIN Cody cody ON cc.cody.codyId = cody.codyId " +
            "LEFT JOIN Like l ON cody.codyId = l.cody.codyId " +
            "WHERE (:searchText IS NULL OR cody.title LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.category = :category) " +
            "GROUP BY c.clothingId")
    List<Object[]> searchClothingByCodyTitle(@Param("searchText") String searchText,
                                             @Param("category") String category);

    // Cody 테이블을 통한 의상 검색 - 설명 기준
    @Query("SELECT DISTINCT c, COUNT(l) as likeCount FROM Clothing c " +
            "JOIN CodyClothes cc ON c.clothingId = cc.clothing.clothingId " +
            "JOIN Cody cody ON cc.cody.codyId = cody.codyId " +
            "LEFT JOIN Like l ON cody.codyId = l.cody.codyId " +
            "WHERE (:searchText IS NULL OR cody.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.category = :category) " +
            "GROUP BY c.clothingId")
    List<Object[]> searchClothingByCodyDescription(@Param("searchText") String searchText,
                                                   @Param("category") String category);

    // Cody 테이블을 통한 의상 검색 - 제목과 설명 모두
    @Query("SELECT DISTINCT c, COUNT(l) as likeCount FROM Clothing c " +
            "JOIN CodyClothes cc ON c.clothingId = cc.clothing.clothingId " +
            "JOIN Cody cody ON cc.cody.codyId = cody.codyId " +
            "LEFT JOIN Like l ON cody.codyId = l.cody.codyId " +
            "WHERE (:searchText IS NULL OR cody.title LIKE %:searchText% OR cody.description LIKE %:searchText%) AND " +
            "(:category IS NULL OR c.category = :category) " +
            "GROUP BY c.clothingId")
    List<Object[]> searchClothingByCodyTitleAndDescription(@Param("searchText") String searchText,
                                                           @Param("category") String category);

    // 모든 카테고리 조회 (Cody에 포함된 의상들만)
    @Query("SELECT DISTINCT c.category FROM Clothing c " +
            "JOIN CodyClothes cc ON c.clothingId = cc.clothing.clothingId " +
            "WHERE c.category IS NOT NULL ORDER BY c.category")
    List<String> findDistinctCategoriesFromCody();
}