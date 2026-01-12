package com.amp.domain.festival.dto.response;

import com.amp.domain.festival.entity.Festival;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FestivalCreateResponse {
    private Long festivalId;
    private String mainImageUrl;
    private String title;
    private LocalDateTime createdAt;

    public static FestivalCreateResponse from(Festival festival) {
        return FestivalCreateResponse.builder()
                .festivalId(festival.getId())
                .title(festival.getTitle())
                .mainImageUrl(festival.getMainImageUrl())
                .createdAt(festival.getCreatedAt())
                .build();
    }
}
