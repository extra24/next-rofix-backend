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

import java.util.Collections;
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

        // 새로운 세션 생성 (세션이 없으면 새로 만들고, 있으면 기존 세션을 사용)
        HttpSession session = request.getSession(true);

        // UserService를 통해 DB에서 User 정보를 가져오거나 새로 생성합니다.
        // 이 과정에서 세션에 유저 정보를 명확하게 등록합니다.
        User user = userService.handleUserLogin(email, session);

        // User 객체를 UserDTO로 변환하여 클라이언트에 필요한 정보만 전달합니다.
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
        HttpSession session = request.getSession();
        User user = userService.handleUserLogin(email, session);

        return ResponseEntity.ok("로그인 성공!" + user);
    }

    // 로그인 상태를 확인하는 API (프런트엔드에서 세션 유효성 체크용)
    @GetMapping("/check")
    public ResponseEntity<?> checkLogin(HttpSession session) {
        try {
            // 세션에서 'user' 객체를 가져옴
            Object userObj = session.getAttribute("user");
            log.info("세션에서 user 객체 확인 : {}", userObj);

            // user 객체가 존재하고, 타입이 User인지 확인
            if (userObj instanceof User user) {
                // 세션에서 가져온 User 객체를 UserDTO로 변환하여 반환합니다.
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
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공!");
    }
}
