package com.amp.domain.wishList.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record WishListPageResponse(
        List<WishListSummaryResponse> festivals,
        PaginationResponse pagination
) {
    public static WishListPageResponse of(Page<WishListSummaryResponse> page) {
        return new WishListPageResponse(page.getContent(), PaginationResponse.from(page));
    }

    public boolean isEmpty() {
        return festivals().isEmpty();
    }
}
