package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.dto.ClothingDTO;
import com.rofix.fitspot.webservice.api.user.dto.ClothingMapper;
import com.rofix.fitspot.webservice.api.user.dto.ClothingRequestDTO;
import com.rofix.fitspot.webservice.api.user.entity.Clothing;
import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.repository.ClothingRepository;
import com.rofix.fitspot.webservice.api.user.repository.UserRepository;
import com.rofix.fitspot.webservice.aws.S3Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ClothingService {

    private final ClothingRepository clothingRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    // 옷 전체 조회
    public List<ClothingDTO> getAllClothes() {
        return clothingRepository.findAll()
                .stream()
                .map(ClothingMapper::toDTO)
                .toList();
    }

    // userId 별 옷 조회
    public List<ClothingDTO> getClothesByUserID(Long userId) {
        return clothingRepository.findByUserUserId(userId)
                .stream()
                .map(ClothingMapper::toDTO)
                .toList();
    }

    // 옷 생성
    public ClothingDTO createClothing(ClothingRequestDTO dto, MultipartFile file, Long userId) throws IOException {
        String key = null;
        try {
            // 1. userId로 User 엔티티를 조회
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            // 2. Mapper를 사용하여 엔티티 생성 (User 정보는 아직 없음)
            Clothing clothing = ClothingMapper.toEntity(dto);

            // 3. 조회한 User 객체를 Clothing 엔티티에 직접 설정
            clothing.setUser(user);

            if (file != null && !file.isEmpty()) {
                key = s3Service.uploadFile(file, userId);
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
