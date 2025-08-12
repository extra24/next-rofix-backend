package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User handleUserLogin(String email, HttpSession session) {
        // 1. 이메일로 회원 찾기. 없으면 새로 생성
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    // 회원이 없으면 새 User 객체 생성
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setNickname("user_" + UUID.randomUUID().toString().substring(0, 8));
                    newUser.setCreatedAt(LocalDateTime.now());
                    return userRepository.save(newUser); // DB에 저장하고 반환
                });

        // 2. 세션에 사용자 정보 저장
        session.setAttribute("user", user);

        return user;

    }

}
