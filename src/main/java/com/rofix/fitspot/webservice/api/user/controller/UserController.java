package com.rofix.fitspot.webservice.api.user.controller;

import com.rofix.fitspot.webservice.api.user.dto.UserDTO;
import com.rofix.fitspot.webservice.api.user.entity.User;
import com.rofix.fitspot.webservice.api.user.mapper.UserMapper;
import com.rofix.fitspot.webservice.api.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    // 현재 로그인된 사용자의 정보를 조회
    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ResponseEntity<>("로그인 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        Optional<User> userInfo = userService.findUserById(user.getUserId());
        return userInfo
                .map(UserMapper::toDTO) // UserMapper의 정적 메서드를 사용
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 회원 정보 수정
    @PutMapping
    public ResponseEntity<?> updateMyInfo(HttpSession session,
                                          @RequestBody Map<String, String> payload) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ResponseEntity<>("로그인 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            String email = payload.get("email");
            String nickname = payload.get("nickname");
            String personalColor = payload.get("personalColor");

            User updatedUser = userService.updateUser(user.getUserId(), email, nickname, personalColor);
            session.setAttribute("user", updatedUser);

            UserDTO updatedUserDto = UserMapper.toDTO(updatedUser); // UserMapper의 정적 메서드를 사용
            return ResponseEntity.ok(updatedUserDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 정보 업데이트에 실패했습니다.");
        }
    }

    // 회원 탈퇴
    @DeleteMapping
    public ResponseEntity<?> deleteMyInfo(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return new ResponseEntity<>("로그인 정보가 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        try {
            userService.deleteUser(user.getUserId(), session);
            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 탈퇴에 실패했습니다.");
        }
    }
}
