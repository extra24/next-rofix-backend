package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.repository.ClothingRepository;
import com.rofix.fitspot.webservice.aws.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Slf4j
public class ClothingService {

    @Autowired
    private ClothingRepository clothingRepository;

    @Autowired
    private S3Service s3Service;

    // 옷 전체 조회
    public List<Clothing> getAllClothes() {return clothingRepository.findAll();}

    // userId 별 옷 조회
    public List<Clothing> getClothesByUserID(Long userId) {
        return clothingRepository.findByUserUserId(userId);
    }

    // 옷 생성
    public Clothing createClothing(Clothing clothing, MultipartFile file) throws IOException {
        String key = null;
        try {
            if (file != null && !file.isEmpty()) {
                key = s3Service.uploadFile(file, clothing.getUser().getUserId());
                clothing.setImageKey(key);
                clothing.setImageUrl(s3Service.getUrlForKey(key));
            }
            return clothingRepository.save(clothing);
        } catch (Exception e) {
            if (key != null) {
                try {
                    s3Service.deleteFile(key);
                } catch (Exception ex) {
                    log.warn("S3 rollback failed for key {}", key, ex);
                }
            }
            throw e;
        }
    }

    // 옷 삭제
    public void deleteClothing(Long clothingId) {
        clothingRepository.findById(clothingId).ifPresent( clothing -> {
            if (clothing.getImageKey() != null) {
                try {
                    s3Service.deleteFile(clothing.getImageKey());
                } catch (Exception e) {
                    log.warn("S3 delete failed for key {}", clothing.getImageKey(), e);
                }
            }
            clothingRepository.deleteById(clothingId);
        });
    }
}
