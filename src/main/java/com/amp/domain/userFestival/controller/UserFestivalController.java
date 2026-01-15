package com.amp.domain.userFestival.controller;

import com.amp.domain.userFestival.dto.request.WishListRequest;
import com.amp.domain.userFestival.dto.response.UserFestivalPageResponse;
import com.amp.domain.userFestival.dto.response.WishListResponse;
import com.amp.domain.userFestival.service.UserFestivalService;
import com.amp.global.common.SuccessStatus;
import com.amp.global.response.success.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/festivals")
public class UserFestivalController {

    private final UserFestivalService userFestivalService;

    @GetMapping
    public ResponseEntity<BaseResponse<UserFestivalPageResponse>> getAllFestivalLists(
            @PageableDefault(size = 20) Pageable pageable) {

        UserFestivalPageResponse response = userFestivalService.getAllFestivalLists(pageable);

        SuccessStatus status = response.isEmpty()
                ? SuccessStatus.FESTIVAL_NOT_FOUND
                : SuccessStatus.FESTIVAL_LIST_FOUND;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }

    @PutMapping("{festivalId}/wishList")
    public ResponseEntity<BaseResponse<WishListResponse>> toggleWishList(
            @PathVariable Long festivalId,
            @RequestBody WishListRequest request
    ) {
        WishListResponse response = userFestivalService.toggleWishlist(festivalId, request);

        SuccessStatus status = response.wishlist()
                ? SuccessStatus.WISHLIST_ADDED
                : SuccessStatus.WISHLIST_REMOVED;

        return ResponseEntity
                .status(status.getHttpStatus())
                .body(BaseResponse.ok(status.getMsg(), response));
    }
}
