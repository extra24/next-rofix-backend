package com.rofix.fitspot.webservice.api.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, name = "ip_address")
    private String ipAddress;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "personal_color")
    private String personalColor;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    // Clothes와 관계 1:N
    @OneToMany(mappedBy = "user")
    private List<Clothing> clothes;

    // Cody와 관계

    // Comments와 관계

    // Likes와 관계

}
