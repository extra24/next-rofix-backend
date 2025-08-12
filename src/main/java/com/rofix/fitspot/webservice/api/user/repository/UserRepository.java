package com.rofix.fitspot.webservice.api.user.repository;

import com.rofix.fitspot.webservice.api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
