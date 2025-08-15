package com.rofix.fitspot.webservice.api.user.repository;

import com.rofix.fitspot.webservice.api.user.entity.CodyClothes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodyClothesRepository extends JpaRepository<CodyClothes, Long> {

    // 특정 코디에 속한 모든 의상-코디 관계 조회
    List<CodyClothes> findByCodyCodyId(Long codyId);

    // 특정 의상이 포함된 모든 코디 조회
    List<CodyClothes> findByClothingClothingId(Long clothingId);

    // 특정 코디에서 특정 카테고리의 의상 조회
    @Query("SELECT cc FROM CodyClothes cc " +
            "JOIN cc.clothing c " +
            "WHERE cc.cody.codyId = :codyId " +
            "AND c.category = :category")
    List<CodyClothes> findByCodyIdAndClothingCategory(@Param("codyId") Long codyId,
                                                      @Param("category") String category);
}