package com.amp.domain.notification.dto.request;

public record FcmTopicMessageRequest(
     String topic,
     String title,
     String body
) {
}
