package com.rofix.fitspot.webservice.api.user.service;

import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
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

    public Optional<User> findUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // 닉네임 수정
    public User updateNickname(Long userId, String newNickname) {
        if (newNickname == null || newNickname.isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 입력 항목입니다.");
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setNickname(newNickname);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 퍼스널컬러만 업데이트
    public User updatePersonalColor(Long userId, String newPersonalColor) {
        if (newPersonalColor == null || newPersonalColor.isEmpty()) {
            throw new IllegalArgumentException("퍼스널컬러는 필수 입력 항목입니다.");
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setPersonalColor(newPersonalColor);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    }

    // 이메일만 수정
    public User updateEmail(Long userId, String newEmail) {
        if (newEmail == null || newEmail.isEmpty()) {
            throw new IllegalArgumentException("닉네임은 필수 입력 항목입니다.");
        }

        return userRepository.findById(userId)
                .map(user -> {
                    user.setEmail(newEmail);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 회원 정보 수정 (이메일, 닉네임, 퍼스널컬러)
    public User updateUser(Long userId, String email, String nickname, String personalColor) {
        return userRepository.findById(userId)
                .map(user -> {
                    if (email != null && !email.isEmpty()) {
                        user.setEmail(email);
                    }
                    if (nickname != null && !nickname.isEmpty()) {
                        user.setNickname(nickname);
                    }
                    if (personalColor != null && !personalColor.isEmpty()) {
                        user.setPersonalColor(personalColor);
                    }
                    // 변경된 정보 저장 후 반환
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser(Long userId, HttpSession session) {
        // 1. 해당 사용자가 존재하는지 확인
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("사용자를 찾을 수 없습니다.");
        }

        // 2. 현재 로그인된 사용자와 탈퇴 요청 사용자가 동일한지 확인 (보안 강화)
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || !currentUser.getUserId().equals(userId)) {
            throw new SecurityException("권한이 없습니다. 본인 계정만 탈퇴할 수 있습니다.");
        }

        // 3. 회원 탈퇴 실행
        userRepository.deleteById(userId);

        // 4. 세션 무효화
        session.invalidate();
    }
}
