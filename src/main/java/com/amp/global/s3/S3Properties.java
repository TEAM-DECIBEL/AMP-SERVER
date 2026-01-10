package com.amp.global.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.cloud.aws.s3")
@Component
public class S3Properties {
    private String bucket;
    private String region;
    private String baseUrl;
}
