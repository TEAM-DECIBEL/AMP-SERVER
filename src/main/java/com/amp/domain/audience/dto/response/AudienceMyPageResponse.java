package com.amp.domain.audience.dto.response;

import com.amp.domain.user.entity.User;
import com.amp.domain.user.entity.UserType;
import lombok.Builder;

@Builder
public record AudienceMyPageResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        UserType userType
) {
    public static AudienceMyPageResponse from(User user) {
        return AudienceMyPageResponse.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .userType(user.getUserType())
                .build();
    }
}
