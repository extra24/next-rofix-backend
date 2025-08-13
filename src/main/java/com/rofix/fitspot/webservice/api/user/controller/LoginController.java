package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.entity.UserDTO;
import com.rofix.fitspot.webservice.api.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class LoginController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?>  login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        String email = payload.get("email");
        if (email == null || email.isEmpty()) {
            return ResponseEntity.badRequest().body("이메일이 누락되었습니다.");
        }
        HttpSession session = request.getSession();
        User user = userService.handleUserLogin(email, session);

        // 세션에 저장
        session.setAttribute("user", user);

        // DTO 변환
        UserDTO dto = new UserDTO(
                user.getUserId(),
                user.getEmail(),
                user.getNickname(),
                user.getPersonalColor(),
                user.getCreatedAt()
        );

        return ResponseEntity.ok(Map.of(
                "message", "로그인 성공",
                "user", dto
        ));
    }

    // 로그인 상태를 확인하는 API (프런트엔드에서 세션 유효성 체크용)
    @GetMapping("/check")
    public ResponseEntity<?> checkLogin(HttpSession session) {
        // 세션에서 'user' 객체를 가져옴
        Object userObj = session.getAttribute("user");
        log.info("세션에서 user 객체 확인 : {}", userObj);

        // user 객체가 존재하고, 타입이 User인지 확인
        if (userObj instanceof User user) {
            // 사용자 정보가 유효하면 DTO로 변환하여 반환
            UserDTO dto = new UserDTO(
                    user.getUserId(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getPersonalColor(),
                    user.getCreatedAt()
            );
            return ResponseEntity.ok(Map.of("user", dto));
        } else {
            // user 객체가 없거나 유효하지 않으면 401 Unauthorized 응답 반환
            return ResponseEntity.status(401).body(Map.of("user", null));
        }
    }

    // 로그아웃을 처리하는 API
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공!");
    }
}
