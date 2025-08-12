package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    // 회원 정보 조회
    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInfo(@PathVariable("userId") Long userId) {
        return userService.findUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 닉네임만 수정
    @PutMapping("/{userId}/nickname")
    public ResponseEntity<?> updateNickname(@PathVariable("userId") Long userId,
                                            @RequestBody Map<String, String> payload) {
        try {
            String newNickname = payload.get("nickname");
            User updatedUser = userService.updateNickname(userId, newNickname);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("닉네임 업데이트에 실패했습니다.");
        }
    }

    // 퍼스널컬러만 수정
    @PutMapping("/{userId}/personalColor")
    public ResponseEntity<?> updatePersonalColor(@PathVariable("userId") Long userId,
                                                 @RequestBody Map<String, String> payload) {
        try {
            String newPersonalColor = payload.get("personalColor");
            User updatedUser = userService.updatePersonalColor(userId, newPersonalColor);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("퍼스널컬러 업데이트에 실패했습니다.");
        }
    }


    // 이메일만 수정
    @PutMapping("/{userId}/email")
    public ResponseEntity<?> updateEmail(@PathVariable("userId") Long userId,
                                                 @RequestBody Map<String, String> payload) {
        try {
            String newEmail = payload.get("email");
            User updatedUser = userService.updateEmail(userId, newEmail);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("퍼스널컬러 업데이트에 실패했습니다.");
        }
    }


    // 회원 정보 수정
    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUserInfo(@PathVariable("userId") Long userId,
                                            @RequestBody Map<String, String> payload) {
        try {
            String email = payload.get("email");
            String nickname = payload.get("nickname");
            String personalColor = payload.get("personalColor");

            User updatedUser = userService.updateUser(userId, email, nickname, personalColor);
            return ResponseEntity.ok(updatedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 정보 업데이트에 실패했습니다.");
        }
    }

    // 회원 탈퇴
    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable("userId") Long userId, HttpSession session) {
        try {
            userService.deleteUser(userId, session);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("회원 탈퇴에 실패했습니다.");
        }
    }
}
