package com.example.capstone.community.controller;

import com.example.capstone.community.dto.CreatePostReqDto;
import com.example.capstone.community.dto.FullPostResDto;
import com.example.capstone.community.dto.PagingPostListResDto;
import com.example.capstone.community.dto.PostListResDto;
import com.example.capstone.community.service.PostService;
import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.Province;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/community/post")
public class PostController {

    private final PostService postService;

    @Operation(summary = "게시글 생성", description = "게시글 생성시 제목, 내용, 목적지, 여러개의 이미지를 포함해 생성 가능")
    @PostMapping("/create")
    public ResponseEntity<?> createPost(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                        @Valid @RequestPart("postInfo")CreatePostReqDto createPostReqDto,
                                        @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages) throws JsonProcessingException {
        Long postId = postService.createPost(userDetails, createPostReqDto, postImages);
        return new ResponseEntity<>(postId, HttpStatus.OK);
    }

    @Operation(summary = "게시글 삭제", description = "자신이 작성한 게시글 삭제")
    @DeleteMapping("/delete/{postId}")
    public ResponseEntity<?> deletePost(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                        @PathVariable Long postId) {
        postService.deletePost(userDetails, postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "게시글 상세 조회", description = "사용자가 선택한 게시글의 전체 내용을 조회")
    @GetMapping("/full/{postId}")
    public ResponseEntity<?> getFullPost(@PathVariable Long postId) throws JsonProcessingException {

        FullPostResDto fullPostResDto = postService.getFullPost(postId);
        return new ResponseEntity<>(fullPostResDto, HttpStatus.OK);
    }

    @Operation(summary = "게시글 수정", description = "자신이 작성한 게시글 수정")
    @PutMapping("/edit/{postId}")
    public ResponseEntity<?> editPost(@AuthenticationPrincipal CustomOAuth2User userDetails,
                                      @Valid @RequestPart("postInfo")CreatePostReqDto createPostReqDto,
                                      @RequestPart(value = "postImages", required = false) List<MultipartFile> postImages,
                                      @PathVariable Long postId) throws JsonProcessingException {
        postService.editPost(userDetails, createPostReqDto, postImages, postId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "게시글 리스트 조회", description = "커뮤니티 메인 화면에 나타날 게시글 리스트 조회")
    @GetMapping("/list")
    public ResponseEntity<?> getPostList(@PageableDefault(size = 20, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable) {
        PagingPostListResDto postList = postService.getPostList(pageable);
        return new ResponseEntity<>(postList, HttpStatus.OK);
    }

    @Operation(summary = "게시글 필터 조회", description = "제목, 목적지 기반 게시글 리스트 필터 조회")
    @GetMapping("/filter/list")
    public ResponseEntity<?> getFilterPostList(@PageableDefault(size = 20, sort = "createdTime", direction = Sort.Direction.DESC) Pageable pageable,
                                               @RequestParam("province") Province province,
                                               @RequestParam("city")City city,
                                               @RequestParam("title") String title) {
        PagingPostListResDto postFilterList = postService.getFilterPostList(pageable, title, province, city);
        return new ResponseEntity<>(postFilterList, HttpStatus.OK);
    }
}
