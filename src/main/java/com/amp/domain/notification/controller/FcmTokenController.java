package com.amp.domain.notification.controller;

import com.amp.domain.notification.dto.request.FcmTopicSubscribeRequest;
import com.amp.domain.notification.service.CategorySubscribeService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Audience API")
@RequestMapping("/api/v1/audience/notifications")
public class FcmTokenController {

    private final CategorySubscribeService categorySubscribeService;

    @Operation(summary = "FCM 토큰 기기 동기화", description = "로그인한 기기의 FCM 토큰을 현재 구독 중인 모든 카테고리 토픽에 등록합니다.")
    @PostMapping("/fcm-token")
    public ResponseEntity<BaseResponse<Void>> registerFcmToken(
            @RequestBody @Valid FcmTopicSubscribeRequest fcmTopicSubscribeRequest
    ) {
        categorySubscribeService.registerToken(fcmTopicSubscribeRequest.fcmToken());
        return ResponseEntity
                .status(SuccessStatus.SUBSCRIBE_SUCCESS.getHttpStatus())
                .body(BaseResponse.ok(SuccessStatus.SUBSCRIBE_SUCCESS.getMsg(), null));
    }
}
