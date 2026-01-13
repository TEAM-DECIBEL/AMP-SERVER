package com.amp.domain.notice.dto.request;

import jakarta.validation.constraints.NotNull;

public record NoticeSaveRequest(
        @NotNull
        boolean isBookmarked
) {
}
