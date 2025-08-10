package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.service.ClothingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clothes")
public class ClothingController {

    @Autowired
    private ClothingService clothingService;

    // 전체 옷 조회
    @GetMapping
    public List<Clothing> getAllClothes() {return clothingService.getAllClothes();}

    // userId 별 옷 조회
    @GetMapping("/user/{userId}")
    public List<Clothing> getAllClothes (@PathVariable Long userId) {
        return clothingService.getClothesByUserID(userId);
    }

    // 옷 생성 - s3 로직


    // 옷 삭제 - s3 로직 적용
}
