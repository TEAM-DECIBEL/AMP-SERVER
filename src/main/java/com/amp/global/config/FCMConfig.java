package com.amp.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

@Slf4j
@Configuration
public class FCMConfig {
    // 서버용
    @Value("${fcm.config-64:}")
    private String base64Config;

    // 로컬용
    @Value("${fcm.key.path}")
    private String serviceAccountJson;

    @PostConstruct
    public void init() {
        try {
            if (!FirebaseApp.getApps().isEmpty()) return;

            InputStream serviceAccount;

            if (base64Config != null && !base64Config.trim().isEmpty()) {
                byte[] decodedBytes = Base64.getDecoder().decode(base64Config.trim());
                serviceAccount = new ByteArrayInputStream(decodedBytes);
            } else {
                ClassPathResource resource = new ClassPathResource(serviceAccountJson);
                serviceAccount = resource.getInputStream();
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("파이어베이스 서버 연결");
        } catch (IOException e) {
            log.error("파이어베이스 연결 실패: {}", e.getMessage());
        }
    }
}
