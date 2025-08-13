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
        Object userObj = session.getAttribute("user");

        // user 객체가 존재하면
        if (userObj instanceof User user) {
            UserDTO userDTO = UserMapper.toDTO(user);
            return ResponseEntity.ok(Map.of("user", userDTO));
        } else {
            // user 객체가 없으면, 401 대신 200 OK 응답을 보내고 user: null을 포함
            // 콘솔에 401 에러 뜨는 것 방지
            return ResponseEntity.ok(Collections.singletonMap("user", null));
        }
    }

    // 로그아웃을 처리하는 API
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공!");
    }
}
