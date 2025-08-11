package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.dto.ClothingDTO;
import com.rofix.fitspot.webservice.api.user.dto.ClothingMapper;
import com.rofix.fitspot.webservice.api.user.dto.ClothingRequestDTO;
import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.repository.ClothingRepository;
import com.rofix.fitspot.webservice.aws.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ClothingService {

    @Autowired
    private ClothingRepository clothingRepository;

    @Autowired
    private S3Service s3Service;

    // 옷 전체 조회
    public List<ClothingDTO> getAllClothes() {
        return clothingRepository.findAll()
                .stream()
                .map(ClothingMapper::toDTO)
                .collect(Collectors.toList());
    }

    // userId 별 옷 조회
    public List<ClothingDTO> getClothesByUserID(Long userId) {
        return clothingRepository.findByUserUserId(userId)
                .stream()
                .map(ClothingMapper::toDTO)
                .collect(Collectors.toList());
    }

    // 옷 생성
    public ClothingDTO createClothing(ClothingRequestDTO dto, MultipartFile file) throws IOException {
        String key = null;
        try {
            Clothing clothing = ClothingMapper.toEntity(dto);

            if (file != null && !file.isEmpty()) {
                key = s3Service.uploadFile(file, dto.getUserId());
                clothing.setImageKey(key);
                clothing.setImageUrl(s3Service.getUrlForKey(key));
            }
            Clothing saved = clothingRepository.save(clothing);
            return ClothingMapper.toDTO(saved);
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
