package com.amp.domain.organizer.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "주최사 마이페이지 응답")
public record OrganizerMypageResponse(
        @Schema(description = "주최사 이름", example = "주최사 이름")
        String organizerName,

        @Schema(description = "프로필 이미지 URL", example = "https://example.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "진행 중인 공연 수", example = "3")
        Integer ongoingFestivalCount,

        @Schema(description = "진행 예정 공연 수", example = "1")
        Integer upcomingFestivalCount
) {
}