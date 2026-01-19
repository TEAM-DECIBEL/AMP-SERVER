package com.amp.domain.notification.dto;

public record FcmTopicMessageRequest(
     String topic,
     String title,
     String body
) {
}
