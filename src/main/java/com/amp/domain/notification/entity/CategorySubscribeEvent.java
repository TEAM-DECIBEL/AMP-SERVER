package com.amp.domain.notification.entity;

public record CategorySubscribeEvent(
        Long categoryId,
        String fcmToken,
        boolean subscribe // true = 구독, false = 구독안함
) {}
