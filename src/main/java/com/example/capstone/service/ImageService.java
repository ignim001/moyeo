package com.example.capstone.service;

import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.capstone.config.S3Config;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Config s3Config;
    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String imageUpload(MultipartFile file) {

        String fileName = UUID.randomUUID() + "_"+ file.getOriginalFilename();

        // 업로드할 파일 메타정보 생성
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            s3Config.amazonS3Client().putObject(new PutObjectRequest(bucketName, fileName, inputStream, metadata));
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 실패", e);
        }

        return s3Config.amazonS3Client().getUrl(bucketName, fileName).toString();
    }

    public void deleteImage(String imageUrl) {
        String fileKey = imageUrl.substring(imageUrl.indexOf(".com/") + 5);
        s3Config.amazonS3Client().deleteObject(new DeleteObjectRequest(bucketName, fileKey));
    }
}
