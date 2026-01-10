package com.amp.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/tests3")
public class S3TestController {

    private final S3Service s3Service;

    @PostMapping("/upload")
    public String upload(@RequestParam MultipartFile file) {
        return s3Service.upload(file, "test");
    }

    @DeleteMapping("/delete")
    public void delete(@RequestParam String key) {
        s3Service.delete(key);
    }

    @GetMapping("/url")
    public String getUrl(@RequestParam String key) {
        return s3Service.getPublicUrl(key);
    }
}
