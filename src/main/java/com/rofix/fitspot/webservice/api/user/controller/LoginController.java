package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
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

        return ResponseEntity.ok("로그인 성공!" + user);
    }

    // 로그인 상태를 확인하는 API (프런트엔드에서 세션 유효성 체크용)
    @GetMapping("/check")
    public ResponseEntity<?> checkLogin(HttpSession session) {
        if (session.getAttribute("user") != null) {
            return ResponseEntity.ok("로그인되어 있습니다.");
        } else {
            return ResponseEntity.status(401).body("로그인이 필요합니다.");
        }
    }
}
