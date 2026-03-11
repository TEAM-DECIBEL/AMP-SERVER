package com.amp.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record NoticeUpdateRequest(
        @NotNull(message = "페스티벌 아이디는 필수값입니다.") Long festivalId,
        @NotBlank(message = "공지 제목은 필수값입니다.")
        @Size(max = 50, message = "공지 제목은 최대 50자까지 입력할 수 있습니다.") String title,
        @NotNull(message = "공지 카테고리 값은 필수값입니다.") Long categoryId,
        List<String> keepImageUrls,
        @NotBlank(message = "공지 내용은 필수값입니다.") String content,
        @NotNull(message = "고정 여부 값은 필수 값입니다.") Boolean isPinned
) {
}
