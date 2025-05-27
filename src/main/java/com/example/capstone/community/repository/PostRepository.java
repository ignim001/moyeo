package com.example.capstone.community.repository;

import com.example.capstone.community.entity.Post;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
    Optional<Post> findByUserIdAndId(Long user_id, Long id);
    @Override
    @EntityGraph(attributePaths = "userEntity")
    Optional<Post> findById(Long aLong);
}
