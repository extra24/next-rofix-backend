package com.rofix.fitspot.webservice.api.user.mapper;

import com.rofix.fitspot.webservice.api.user.dto.UserDTO;
import com.rofix.fitspot.webservice.api.user.entity.User;

public final class UserMapper {

    // 외부에서 인스턴스 생성을 막기 위해 private 생성자 추가
    private UserMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getPersonalColor(),
                user.getCreatedAt()
        );
    }

    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getNickname());
        user.setPersonalColor(dto.getPersonalColor());
        user.setCreatedAt(dto.getCreatedAt());
        return user;
    }
}
