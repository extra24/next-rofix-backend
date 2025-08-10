package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.repository.ClothingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClothingService {

    @Autowired
    private ClothingRepository clothingRepository;

    // 옷 전체 조회
    public List<Clothing> getAllClothes() {return clothingRepository.findAll();}

    // userId 별 옷 조회
    public List<Clothing> getClothesByUserID(Long userId) {
        return clothingRepository.findByUserUserId(userId);
    }

    // 옷 생성 - s3 고려 필요
    public Clothing createClothing(Clothing clothing) {
        // @TODO S3 업로드 로직 필요
        return clothingRepository.save(clothing);
    }

    // 옷 삭제 @TODO S3에서 해당 이미지 파일 삭제로직도 같이 필요
    public void deleteClothing(Long id) {clothingRepository.deleteById(id);}
}
