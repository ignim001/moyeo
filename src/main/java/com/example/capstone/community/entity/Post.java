package com.example.capstone.community.entity;

import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.Province;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.util.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Province province;

    @Enumerated(EnumType.STRING)
    private City city;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(name = "image_uris", columnDefinition = "TEXT")
    private String imageUris;

    public void updatePost(String title, String content, String imageUris, City city, Province province) {
        this.title = title;
        this.content = content;
        this.imageUris = imageUris;
        this.city = city;
        this.province = province;
    }
}
