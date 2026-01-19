package com.amp.domain.wishList.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record WishListHistoryPageResponse(
        List<WishListHistoryResponse> festivals,
        PaginationResponse pagination
) {
    public static WishListHistoryPageResponse of(Page<WishListHistoryResponse> page) {
        return new WishListHistoryPageResponse(
                page.getContent(),
                PaginationResponse.from(page)
        );
    }
}