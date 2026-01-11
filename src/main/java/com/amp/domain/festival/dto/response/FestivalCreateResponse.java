package com.amp.domain.festival.dto.response;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class FestivalCreateResponse {
    private Long festivalId;
    private String mainImageUrl;
    private String title;
    private LocalDateTime createAt;
}
