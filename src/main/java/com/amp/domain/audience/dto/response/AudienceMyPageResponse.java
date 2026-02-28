package com.amp.domain.audience.dto.response;

import com.amp.domain.user.entity.Audience;
import com.amp.domain.user.entity.UserType;
import lombok.Builder;

@Builder
public record AudienceMyPageResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        UserType userType
) {
    public static AudienceMyPageResponse from(Audience audience) {
        return AudienceMyPageResponse.builder()
                .userId(audience.getId())
                .nickname(audience.getNickname())
                .profileImageUrl(audience.getProfileImageUrl())
                .userType(audience.getUserType())
                .build();
    }
}
