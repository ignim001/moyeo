package com.example.capstone.community.controller;

import com.example.capstone.community.dto.CommentListResDto;
import com.example.capstone.community.dto.CreateCommentReqDto;
import com.example.capstone.community.service.CommentService;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/community/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 생성", description = "게시글에 댓글 추가")
    @PostMapping("/create/{postId}")
    public ResponseEntity<?> createComment(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                           @Valid @RequestBody CreateCommentReqDto commentReqDto,
                                           @PathVariable Long postId) {
        commentService.createComment(userDetails, commentReqDto, postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "댓글 삭제", description = "자신이 작성한 댓글 삭제")
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<?> deleteComment(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                           @PathVariable Long commentId) {
        commentService.deleteComment(userDetails, commentId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "댓글 수정", description = "자신이 작성한 댓글 수정")
    @PutMapping("/edit/{commentId}")
    public ResponseEntity<?> editComment(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                         @PathVariable Long commentId,
                                         @Valid @RequestBody CreateCommentReqDto commentReqDto) {
        commentService.editComment(userDetails, commentId, commentReqDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "댓글 리스트 조회", description = "해당 게시글에 작성된 댓글 리스트 조회")
    @GetMapping("/list/{postId}")
    public ResponseEntity<?> getCommentList(@PathVariable Long postId) {

        List<CommentListResDto> commetList = commentService.getCommetList(postId);
        return new ResponseEntity<>(commetList, HttpStatus.OK);
    }
}
