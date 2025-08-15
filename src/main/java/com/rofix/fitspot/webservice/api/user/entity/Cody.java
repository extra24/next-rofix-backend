package com.rofix.fitspot.webservice.api.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "cody")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cody {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cody_id")
    private Long codyId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column
    private String description;

    @Column
    private String weather;

    @Column(unique = true)  //중복 방지용
    private String hash;

    // getter, setter 추가
    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }


    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;

    // Comments와의 관계 1:N
    @OneToMany(mappedBy = "cody")
    private List<Comment> comments;

    // Likes와의 관계: 1:N
    @OneToMany(mappedBy = "cody")
    private List<Like> likes;

    // Cody_Clothes와의 관계: 1:N
    @OneToMany(mappedBy = "cody")
    private List<CodyClothes> codyClothes;
}
