package com.amp.domain.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record NoticeCreateRequest(
        @NotBlank(message = "공지 제목은 필수값입니다.")
        @Size(max = 50, message = "공지 제목은 최대 50자까지 입력할 수 있습니다.") String title,
        @NotBlank(message = "공지 카테고리 값은 필수값입니다.") String categoryId,
        MultipartFile image,
        @NotBlank(message = "공지 제목은 필수값입니다.") String content,
        @NotNull(message = "고정 여부 값은 필수 값입니다.") boolean isPinned
) {
}

