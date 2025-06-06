package com.example.capstone.community.service;

import com.example.capstone.community.dto.CommentListResDto;
import com.example.capstone.community.dto.CreateCommentReqDto;
import com.example.capstone.community.entity.Comment;
import com.example.capstone.community.entity.Post;
import com.example.capstone.community.repository.CommentRepository;
import com.example.capstone.community.repository.PostRepository;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.user.repository.UserRepository;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    // 댓글 생성
    @Transactional
    public void createComment(CustomOAuth2User userDetails, CreateCommentReqDto commentReqDto, Long postId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .comment(commentReqDto.getContent())
                .build();

        commentRepository.save(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(CustomOAuth2User userDetails, Long commentId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Comment comment = commentRepository.findByIdAndUserId(commentId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Comment Not Found"));

        commentRepository.delete(comment);
    }

    // 댓글 수정
    @Transactional
    public void editComment(CustomOAuth2User userDetails, Long commentId, CreateCommentReqDto commentReqDto) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Comment comment = commentRepository.findByIdAndUserId(commentId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Comment Not Found"));

        comment.updateComment(commentReqDto.getContent());
    }

    // 댓글 리스트 조회
    @Transactional(readOnly = true)
    public List<CommentListResDto> getCommetList(Long postId) {
        Optional<List<Comment>> commentList = commentRepository.findByPostIdOrderByCreatedTimeAsc(postId);

        return commentList.map(comments -> comments.stream()
                .map(comment -> CommentListResDto.builder()
                        .commentId(comment.getId())
                        .nickname(comment.getUser().getNickname())
                        .userProfile(comment.getUser().getProfileImageUrl())
                        .comment(comment.getComment())
                        .updatedAt(comment.getUpdatedTime())
                        .build())
                .toList()).orElseGet(ArrayList::new);
    }
}
