package com.amp.domain.wishList.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record MyUpcomingPageResponse(
        List<WishListHistoryResponse> festivals,
        PaginationResponse pagination
) {
    public static MyUpcomingPageResponse of(Page<WishListHistoryResponse> page) {
        return new MyUpcomingPageResponse(
                page.getContent(),
                PaginationResponse.from(page)
        );
    }
}
