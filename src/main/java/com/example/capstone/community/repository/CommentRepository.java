package com.example.capstone.community.repository;

import com.example.capstone.community.entity.Comment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByIdAndUserId(Long commentId, Long id);
    @EntityGraph(attributePaths = "userEntity")
    Optional<List<Comment>> findByPostIdOrderByCreatedTimeAsc(Long postId);
}
