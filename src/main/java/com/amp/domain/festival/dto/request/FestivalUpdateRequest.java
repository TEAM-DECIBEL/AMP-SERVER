package com.amp.domain.festival.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

public record FestivalUpdateRequest(
        @NotBlank(message = "공연명은 필수입니다.") String title,
        @NotBlank(message = "공연 장소는 필수입니다.") String location,
        MultipartFile mainImage,
        @NotBlank(message = "공연 일시는 필수입니다.") String schedules,
        @NotBlank(message = "1개 이상의 무대/부스 정보는 필수입니다.") String stages,
        @NotBlank(message = "1개 이상의 카테고리 선택은 필수입니다.") String activeCategoryIds
) {
}
