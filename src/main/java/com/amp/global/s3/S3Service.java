package com.amp.global.s3;

import com.amp.global.common.CommonErrorCode;
import com.amp.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Properties s3Properties;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${spring.cloud.aws.region.static}")
    private String region;

    public String upload(MultipartFile file, String dir) {
        String contentType = file.getContentType();
        if (contentType == null ||
                !List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new CustomException(CommonErrorCode.INVALID_PROFILE_IMAGE);
        }

        String original = file.getOriginalFilename();
        if (original == null) {
            throw new CustomException(CommonErrorCode.FILE_NAME_NOT_FOUND);
        }

        String ext = StringUtils.getFilenameExtension(original);
        if (ext == null ||
                !List.of("jpg", "jpeg", "png", "webp").contains(ext.toLowerCase())) {
            throw new CustomException(CommonErrorCode.INVALID_PROFILE_IMAGE);
        }

        if (dir.contains("..") || dir.contains("/") || dir.contains("\\")) {
            throw new CustomException(CommonErrorCode.INVALID_DIRECTORY_ROUTE);
        }

        String key = dir + "/" + UUID.randomUUID() + "." + ext;

        try (InputStream is = file.getInputStream()) {
            PutObjectRequest req = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(req, RequestBody.fromInputStream(is, file.getSize()));
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 실패", e);
        }

        return key;
    }

    public void delete(String key) {
        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        s3Client.deleteObject(req);
    }

    public String getPublicUrl(String key) {
        return String.format(
                s3Properties.getBaseUrl(),
                s3Properties.getBucket(),
                s3Properties.getRegion()
        ) + key;
    }
}

