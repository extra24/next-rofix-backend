package com.rofix.fitspot.webservice.api.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(name = "personal_color")
    private String personalColor;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    // Clothes와 관계 1:N
    @OneToMany(mappedBy = "user")
    private List<Clothing> clothes;

    // Cody와 관계
    @OneToMany(mappedBy = "user")
    private List<Cody> cody;

    // Comments와 관계
    @OneToMany(mappedBy = "user")
    private List<Comment> comments;

    // Likes와 관계
    @OneToMany(mappedBy = "user")
    private List<Like> likes;

}
