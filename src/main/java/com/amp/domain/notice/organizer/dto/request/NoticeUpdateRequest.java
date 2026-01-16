package com.amp.domain.notice.organizer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public record NoticeUpdateRequest(
        @NotNull(message = "페스티벌 아이디는 필수값입니다.") Long festivalId,
        @NotBlank(message = "공지 제목은 필수값입니다.")
        @Size(max = 50, message = "공지 제목은 최대 50자까지 입력할 수 있습니다.") String title,
        @NotBlank(message = "공지 카테고리 값은 필수값입니다.") Long categoryId,
        MultipartFile newImage,
        @NotBlank(message = "공지 내용은 필수값입니다.") String content,
        boolean isPinned,
        String previousImageUrl         //기존 첨부이미지에서 이미지 수정 없이 업로드시 기존 이미지url 첨부
) {
}
