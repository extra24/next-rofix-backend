package com.rofix.fitspot.webservice.api.user.repository;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClothingRepository extends JpaRepository<Clothing, Long> {
    List<Clothing> findByUserUserId(Long userid);
}