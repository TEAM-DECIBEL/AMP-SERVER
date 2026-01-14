package com.amp.domain.user.dto;

import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import lombok.Builder;

@Builder
public record MyPageResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        UserType userType
) {
    public static MyPageResponse from(User user) {
        return MyPageResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .userType(user.getUserType())
                .build();
    }
}
