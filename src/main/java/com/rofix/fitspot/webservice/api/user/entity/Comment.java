package com.rofix.fitspot.webservice.api.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    // user 와의 관계 N:1
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // cody 와의 관계 N:1
    @ManyToOne
    @JoinColumn(name = "cody_id")
    private Cody cody;

    @Column
    private String content;

    @Column
    private LocalDateTime created_at;
}
