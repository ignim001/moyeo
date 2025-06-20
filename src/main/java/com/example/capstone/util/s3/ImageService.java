package com.example.capstone.util.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String imageUpload(MultipartFile file, String directoryName) {
        // 디렉토리명을 포함한 파일명 생성
        String fileName = directoryName + "/" + UUID.randomUUID() + "_" + file.getOriginalFilename();

        // 메타데이터 설정
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }

        // 업로드된 이미지의 전체 URL 반환
        return amazonS3Client.getUrl(bucketName, fileName).toString();
    }

    public void deleteImage(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            String rawPath = uri.getPath();
            String fileKey = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;

            // URL 디코딩 (한글 파일 이름 지원)
            fileKey = URLDecoder.decode(fileKey, StandardCharsets.UTF_8.name());
            amazonS3Client.deleteObject(new DeleteObjectRequest(bucketName, fileKey));
        } catch (Exception e) {
            System.err.println("삭제 실패: " + e.getMessage());
        }
    }
}

