package com.amp.domain.festival.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public record FestivalCreateRequest(
        @NotBlank(message = "공연명은 필수입니다.") String title,
        @NotBlank(message = "공연 장소는 필수입니다.") String location,
        @NotNull(message = "공연 이미지는 필수입니다.") MultipartFile mainImageUrl,
        @NotBlank(message = "공연 일시는 필수입니다.") String schedules,
        String stages,
        String activeCategoryIds
) {
}
