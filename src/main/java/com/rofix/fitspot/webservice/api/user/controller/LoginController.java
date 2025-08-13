package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.dto.UserDTO;
import com.rofix.fitspot.webservice.api.user.mapper.UserMapper;
import com.rofix.fitspot.webservice.api.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
@AllArgsConstructor
public class LoginController {

    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>>  login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get("email");
        if (email == null || email.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "이메일이 누락되었습니다.");
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // 새로운 세션 생성 (세션이 없으면 새로 만들고, 있으면 기존 세션을 사용)
        HttpSession session = request.getSession(true);
        User user = userService.handleUserLogin(email, session);

        // Mapper 사용하여 User 객체를 DTO로 변환
        UserDTO userDTO = UserMapper.toDTO(user);

        return ResponseEntity.ok(Map.of(
                "message", "로그인 성공",
                "user", userDTO
        ));
    }

    // 로그인 상태를 확인하는 API (프런트엔드에서 세션 유효성 체크용)
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkLogin(HttpSession session) {
        try {
            // 세션에서 'user' 객체를 가져옴
            Object userObj = session.getAttribute("user");

            // user 객체가 존재하고, 타입이 User인지 확인
            if (userObj instanceof User user) {
                // Mapper 사용해서 User 객체를 DTO로 변환
                UserDTO userDTO = UserMapper.toDTO(user);

                return ResponseEntity.ok(Map.of("user", userDTO));
            } else {
                // user 객체가 없거나 유효하지 않으면 401 Unauthorized 응답 반환
                return ResponseEntity.status(401).body(Collections.singletonMap("user", null));
            }
        } catch (Exception e) {
            // 예상치 못한 예외가 발생하면 로그를 남기고, 401 Unauthorized 응답 반환
            log.error("세션 확인 중 예외 발생", e);
            return ResponseEntity.status(401).body(Collections.singletonMap("user", null));
        }
    }

    // 로그아웃을 처리하는 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공!");
    }
}
