package com.example.capstone.community.service;

import com.example.capstone.community.dto.CreatePostReqDto;
import com.example.capstone.community.dto.FullPostResDto;
import com.example.capstone.community.dto.PagingPostListResDto;
import com.example.capstone.community.dto.PostListResDto;
import com.example.capstone.community.entity.Post;
import com.example.capstone.community.repository.PostRepository;
import com.example.capstone.matching.entity.City;
import com.example.capstone.matching.entity.Province;
import com.example.capstone.user.entity.UserEntity;
import com.example.capstone.user.repository.UserRepository;
import com.example.capstone.util.oauth2.dto.CustomOAuth2User;
import com.example.capstone.util.s3.ImageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ImageService imageService;
    private static final String POST_IMAGE_DIR = "community-image";
    private final ObjectMapper objectMapper;

    // 게시글 생성
    @Transactional
    public Long createPost(CustomOAuth2User userDetails, CreatePostReqDto createPostReqDto, List<MultipartFile> postImages) throws JsonProcessingException {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        List<String> imageUriList = new ArrayList<>();

        if(postImages != null && !postImages.isEmpty()) {
            for (MultipartFile postImage : postImages) {
                String imageUri = imageService.imageUpload(postImage, POST_IMAGE_DIR);
                imageUriList.add(imageUri);
            }
        }

        // Json 배열 직렬화
        String imageUris = objectMapper.writeValueAsString(imageUriList);

        Post post = Post.builder()
                .user(user)
                .title(createPostReqDto.getTitle())
                .content(createPostReqDto.getContent())
                .province(createPostReqDto.getProvince())
                .city(createPostReqDto.getCity())
                .imageUris(imageUris)
                .build();

        Post savedPost = postRepository.save(post);
        return savedPost.getId();
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(CustomOAuth2User userDetails, Long postId) {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Post post = postRepository.findByUserIdAndId(user.getId(), postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));

        postRepository.delete(post);
    }

    // 게시글 내용 조회
    @Transactional(readOnly = true)
    public FullPostResDto getFullPost(Long postId) throws JsonProcessingException {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));

        // Json 배열 역 직렬화
        List<String> imageUriList = objectMapper.readValue(post.getImageUris(), new TypeReference<>() {});

        return FullPostResDto.builder()
                .postId(post.getId())
                .nickname(post.getUser().getNickname())
                .userProfileImage(post.getUser().getProfileImageUrl())
                .title(post.getTitle())
                .content(post.getContent())
                .province(post.getProvince())
                .city(post.getCity())
                .createdDate(post.getCreatedTime())
                .postImages(imageUriList)
                .build();
    }

    @Transactional
    public void editPost(CustomOAuth2User userDetails, CreatePostReqDto createPostReqDto, List<MultipartFile> postImages, Long postId) throws JsonProcessingException {
        UserEntity user = userRepository.findByProviderId(userDetails.getProviderId())
                .orElseThrow(() -> new EntityNotFoundException("User Not Found"));

        Post post = postRepository.findByUserIdAndId(user.getId(), postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));

        // 기존 이미지 S3 삭제
        List<String> imageUriList = objectMapper.readValue(post.getImageUris(), new TypeReference<>() {});
        for (String imageUri : imageUriList) {
            imageService.deleteImage(imageUri);
        }

        List<String> newImageUriList = new ArrayList<>();
        for (MultipartFile postImage : postImages) {
            String imageUri = imageService.imageUpload(postImage, POST_IMAGE_DIR);
            newImageUriList.add(imageUri);
        }

        // Json 배열 직렬화
        String newImageUris = objectMapper.writeValueAsString(newImageUriList);

        post.updatePost(createPostReqDto.getTitle(), createPostReqDto.getContent(), newImageUris, createPostReqDto.getCity(), createPostReqDto.getProvince());
    }

    // 게시글 리스트 조회
    public PagingPostListResDto getPostList(Pageable pageable) {
        Slice<Post> postList = postRepository.findAll(pageable);

        List<PostListResDto> postListResDto = postList.stream()
                .map(post -> PostListResDto.builder()
                        .postId(post.getId())
                        .title(post.getTitle())
                        .nickname(post.getUser().getNickname())
                        .createdAt(post.getCreatedTime())
                        .countComment(post.getComments().size()) // N + 1 문제 발생가능
                        .firstImage(post.getImageUris())
                        .build())
                .toList();

        return PagingPostListResDto.builder()
                .hasNextPage(postList.hasNext())
                .postListResDtos(postListResDto)
                .nowPage(postList.getNumber())
                .build();
    }

    // 게시글 필터 조회
    public PagingPostListResDto getFilterPostList(Pageable pageable, String title, Province province, City city) {
        Slice<PostListResDto> postFilterList = postRepository.findAllByFilter(pageable, title, province, city);

        return PagingPostListResDto.builder()
                .hasNextPage(postFilterList.hasNext())
                .postListResDtos(postFilterList.getContent())
                .nowPage(postFilterList.getNumber())
                .build();
    }

}
