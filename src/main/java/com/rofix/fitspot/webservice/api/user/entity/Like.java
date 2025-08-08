package com.rofix.fitspot.webservice.api.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "likes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long like_id;

    // user 와의 관계 N:1
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // cody 와의 관계 N:1
    @ManyToOne
    @JoinColumn(name = "cody_id")
    private Cody cody;
}
