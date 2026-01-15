package com.amp.domain.userFestival.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record UserFestivalPageResponse(
        List<UserFestivalListResponse> festivals,
        PaginationResponse pagination
) {
    public static UserFestivalPageResponse of(Page<UserFestivalListResponse> page) {
        return new UserFestivalPageResponse(page.getContent(), PaginationResponse.from(page));
    }

    public boolean isEmpty() {
        return festivals == null || festivals().isEmpty();
    }
}
