package com.amp.domain.userFestival.dto.response;

import com.amp.global.common.dto.PaginationResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public record MyWishListPageResponse(
        List<MyWishListResponse> festivals,
        PaginationResponse pagination
) {
    public static MyWishListPageResponse of(Page<MyWishListResponse> page) {
        return new MyWishListPageResponse(
                page.getContent(),
                PaginationResponse.from(page)
        );
    }
}
