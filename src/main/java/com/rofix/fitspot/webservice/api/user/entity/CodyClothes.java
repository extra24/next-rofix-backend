package com.rofix.fitspot.webservice.api.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cody_clothes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodyClothes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cody_clothing_id")
    private Long codyClothingId;

    @ManyToOne
    @JoinColumn(name = "cody_id")
    private Cody cody;

    @ManyToOne
    @JoinColumn(name = "clothing_id")
    private Clothing cloth;
}
